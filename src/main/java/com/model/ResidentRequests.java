package com.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.UUID;
import java.util.function.BooleanSupplier;

/** Resident-owned request changes, with rollback if persistence fails. */
public final class ResidentRequests {
    private static final String MARKER = "\n[HRA_DETAILS_V1]\n";
    private final List<AidRequest> requests;
    private final BooleanSupplier save;

    ResidentRequests(List<AidRequest> requests, BooleanSupplier save) {
        this.requests = requests;
        this.save = save;
    }

    public static boolean belongsTo(AidRequest request, User user) {
        return user != null && user.getUserId() != null && request != null
                && request.getRequester() != null
                && user.getUserId().equals(request.getRequester().getUserId());
    }

    public static boolean canChange(AidRequest request, User user) {
        return belongsTo(request, user) && request.getStatus() == RequestStatus.SUBMITTED
                && request.getAssignments().isEmpty() && request.getShelterId() == null;
    }

    private AidRequest owned(User actor, UUID id) {
        if (requests == null || id == null) { return null; }
        for (AidRequest request : requests) {
            if (belongsTo(request, actor) && id.equals(request.getRequestId())) { return request; }
        }
        return null;
    }

    /** Editing preserves object identity, request ID, timestamps and workflow state. */
    boolean edit(User actor, UUID id, String expectedVersion, RequestCategory category,
                 int people, String address, String note) {
        AidRequest request = owned(actor, id);
        if (!canChange(request, actor) || !Objects.equals(version(request), expectedVersion)
                || category == null || people < 1 || address == null || address.trim().isEmpty()) {
            return false;
        }
        RequestCategory oldCategory = request.getCategory();
        int oldPeople = request.getHouseholdSize();
        String oldAddress = request.getStreetAddress();
        String oldNote = request.getNote();
        request.updateResidentDetails(category, people, address.trim(), note == null ? "" : note);
        boolean saved = false;
        try { saved = save.getAsBoolean(); }
        catch (RuntimeException exception) { saved = false; }
        if (!saved) { request.updateResidentDetails(oldCategory, oldPeople, oldAddress, oldNote); }
        return saved;
    }

    /** Cancelled resident requests use the model's CLOSED state and a persisted note marker. */
    boolean cancel(User actor, UUID id, String expectedVersion) {
        AidRequest request = owned(actor, id);
        if (!canChange(request, actor) || !Objects.equals(version(request), expectedVersion)) { return false; }
        String oldNote = request.getNote();
        Properties details = details(request);
        details.setProperty("cancelledAt", LocalDateTime.now().toString());
        request.updateResidentDetails(request.getCategory(), request.getHouseholdSize(),
                request.getStreetAddress(), encodeDetails(details));
        request.updateStatus(RequestStatus.CLOSED);
        boolean saved = false;
        try { saved = save.getAsBoolean(); }
        catch (RuntimeException exception) { saved = false; }
        if (!saved) {
            request.updateResidentDetails(request.getCategory(), request.getHouseholdSize(),
                    request.getStreetAddress(), oldNote);
            request.updateStatus(RequestStatus.SUBMITTED);
        }
        return saved;
    }

    public static boolean isCancelled(AidRequest request) {
        return request != null && request.getStatus() == RequestStatus.CLOSED
                && !details(request).getProperty("cancelledAt", "").isEmpty();
    }

    /** Detects stale edits and confirmations without trusting UI state. */
    public static String version(AidRequest request) {
        if (request == null) { return ""; }
        String input = request.getCategory() + "\n" + request.getHouseholdSize() + "\n"
                + request.getStreetAddress() + "\n" + request.getNote() + "\n" + request.getStatus()
                + "\n" + request.getAssignments().size() + "\n" + request.getShelterId();
        try {
            return Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-256")
                    .digest(input.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) { throw new IllegalStateException(exception); }
    }

    /** Backward-compatible form data stored in the existing note field. */
    public static Properties details(AidRequest request) {
        Properties data = new Properties();
        if (request == null) { return data; }
        String note = request.getNote() == null ? "" : request.getNote();
        int marker = note.lastIndexOf(MARKER);
        boolean decoded = false;
        if (marker >= 0) {
            try {
                byte[] bytes = Base64.getDecoder().decode(note.substring(marker + MARKER.length()).trim());
                data.load(new ByteArrayInputStream(bytes));
                decoded = true;
            } catch (IllegalArgumentException | IOException exception) { data.clear(); }
        }
        if (!decoded) {
            // Retain the original note verbatim even when importing older form labels.
            data.setProperty("originalNote", note);
            Map<String, String> legacy = new LinkedHashMap<>();
            legacy.put("Requested needs: ", "needs"); legacy.put("Other essentials: ", "otherText");
            legacy.put("Support needed: ", "urgencyText"); legacy.put("Children under 18: ", "children");
            legacy.put("Adults 65 or older: ", "older"); legacy.put("Accessibility / medical supplies: ", "accessibility");
            legacy.put("Situation: ", "situation"); legacy.put("Safe pickup instructions: ", "instructions");
            legacy.put("Preferred contact: ", "contactText"); legacy.put("Contact phone: ", "phone");
            legacy.put("Safe contact times / instructions: ", "safeTime");
            for (String line : note.split("\\R")) {
                for (Map.Entry<String, String> field : legacy.entrySet()) {
                    if (line.startsWith(field.getKey())) { data.setProperty(field.getValue(), line.substring(field.getKey().length())); }
                }
            }
        }
        data.setProperty("people", String.valueOf(request.getHouseholdSize()));
        data.setProperty("address", request.getStreetAddress() == null ? "" : request.getStreetAddress());
        if (data.getProperty("needs", "").isEmpty()) {
            data.setProperty("needs", request.getCategory() == null ? "Relief request"
                    : request.getCategory().name().replace('_', ' ').toLowerCase(java.util.Locale.ROOT));
        }
        return data;
    }

    public static String visibleNote(String note) {
        if (note == null) { return ""; }
        int index = note.lastIndexOf(MARKER);
        return index < 0 ? note : note.substring(0, index);
    }

    /** Structured suffix avoids losing multiline values while keeping a readable summary. */
    public static String encodeDetails(Properties source) {
        Properties data = new Properties(); data.putAll(source);
        if (data.containsKey("other") && !Boolean.parseBoolean(data.getProperty("other"))) { data.remove("otherText"); }
        if (data.getProperty("needs", "").isEmpty()) {
            String[] keys = {"water", "food", "shelter", "medical", "transport", "other"};
            String[] names = {"Drinking water", "Food", "Temporary shelter", "Medical supplies", "Transport", "Other essentials"};
            java.util.ArrayList<String> namesSelected = new java.util.ArrayList<>();
            for (int i = 0; i < keys.length; i++) {
                if (Boolean.parseBoolean(data.getProperty(keys[i], "false"))) { namesSelected.add(names[i]); }
            }
            data.setProperty("needs", String.join(", ", namesSelected));
        }
        if (!data.containsKey("urgencyText")) {
            data.setProperty("urgencyText", "today".equals(data.getProperty("urgency")) ? "Today" : "later".equals(data.getProperty("urgency")) ? "Within the next few days" : "Not recorded");
        }
        if (!data.containsKey("contactText")) {
            data.setProperty("contactText", "sms".equals(data.getProperty("contact")) ? "Text message (SMS)" : "phone".equals(data.getProperty("contact")) ? "Phone call" : "Not recorded");
        }
        StringBuilder readable = new StringBuilder();
        String[][] fields = {{"Requested needs", "needs"}, {"Other essentials", "otherText"},
            {"Support needed", "urgencyText"}, {"Children under 18", "children"}, {"Adults 65 or older", "older"},
            {"Accessibility / medical supplies", "accessibility"}, {"Situation", "situation"},
            {"Safe pickup instructions", "instructions"}, {"Preferred contact", "contactText"},
            {"Contact phone", "phone"}, {"Safe contact times / instructions", "safeTime"},
            {"Details sharing agreement", "sharingAgreed"}, {"Cancelled by resident at", "cancelledAt"}};
        for (String[] field : fields) {
            if (!data.getProperty(field[1], "").isEmpty()) {
                readable.append(field[0]).append(": ").append(data.getProperty(field[1])).append('\n');
            }
        }
        if (!data.getProperty("originalNote", "").isEmpty()) {
            readable.append("Original saved note:\n").append(data.getProperty("originalNote")).append('\n');
        }
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            data.store(bytes, "Resident request form details");
            return readable.toString().trim() + MARKER + Base64.getEncoder().encodeToString(bytes.toByteArray());
        } catch (IOException exception) { throw new IllegalStateException(exception); }
    }
}
