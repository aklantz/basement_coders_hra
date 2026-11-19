package com.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.function.BooleanSupplier;

/** Profile and household operations with local account preferences. */
public final class ResidentAccount {
    private final List<User> users;
    private final BooleanSupplier save;

    ResidentAccount(List<User> users, BooleanSupplier save) { this.users = users; this.save = save; }

    private boolean registered(User user) {
        // Persist the actual current directory object, not an unrelated object with a copied UUID.
        return user != null && user.getUserId() != null && users != null && users.contains(user);
    }

    public static boolean validEmail(String email) {
        return email != null && email.trim().matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+");
    }

    public static String phoneDigits(String input) {
        if (input == null || !input.trim().matches("\\+?[0-9() .-]+")) { return null; }
        String digits = input.replaceAll("[^0-9]", "");
        if (input.trim().startsWith("+") && (digits.length() != 11 || !digits.startsWith("1"))) { return null; }
        if (digits.length() == 11 && digits.startsWith("1")) { digits = digits.substring(1); }
        return digits.length() == 10 ? digits : null;
    }

    boolean updateProfile(User user, String name, String address, String email, String phone) {
        if (!registered(user) || name == null || name.trim().isEmpty() || !validEmail(email)
                || phoneDigits(phone) == null) { return false; }
        for (User other : users) {
            if (other != user && other.getEmailAddress() != null
                    && other.getEmailAddress().equalsIgnoreCase(email.trim())) { return false; }
        }
        String oldName = user.getFullName(), oldAddress = user.getHomeAddress();
        String oldEmail = user.getEmailAddress(), oldPhone = user.getPhoneNumber();
        user.setFullName(name.trim()); user.setHomeAddress(address == null ? "" : address.trim());
        user.setEmailAddress(email.trim()); user.setPhoneNumber(phoneDigits(phone));
        if (persist()) { return true; }
        user.setFullName(oldName); user.setHomeAddress(oldAddress);
        user.setEmailAddress(oldEmail); user.setPhoneNumber(oldPhone);
        return false;
    }

    boolean addHousehold(User user, String name) {
        if (!registered(user) || name == null || name.trim().isEmpty()) { return false; }
        for (Household household : user.getHouseholds()) {
            if (household.getHouseholdName().equalsIgnoreCase(name.trim())) { return false; }
        }
        Household created = new Household(user.getUserId(), name.trim());
        user.getHouseholds().add(created);
        if (persist()) { return true; }
        user.getHouseholds().remove(created);
        return false;
    }

    boolean addMember(User user, String householdName, String name, int age) {
        if (!registered(user) || householdName == null || name == null || name.trim().isEmpty()
                || age < 0 || age > 130) { return false; }
        for (Household household : user.getHouseholds()) {
            if (householdName.equals(household.getHouseholdName())
                    && user.getUserId().equals(household.getHouseholdOwnerUUID())) {
                int originalCount = household.getMembers().size();
                if (!household.addHouseholdMember(name.trim(), age, null, null)) { return false; }
                if (persist()) { return true; }
                household.getMembers().remove(originalCount);
                return false;
            }
        }
        return false;
    }

    private boolean persist() {
        try { return save.getAsBoolean(); }
        catch (RuntimeException exception) { return false; }
    }

    public static Path settingsPath(UUID userId) {
        if (userId == null) { throw new IllegalArgumentException("A signed-in user is required."); }
        return Paths.get(System.getProperty("user.home"), ".hurricane-tracker", "accounts", userId + ".properties");
    }

    public static Properties loadSettings(Path path) throws IOException {
        Properties settings = new Properties();
        if (Files.exists(path)) {
            try (InputStream input = Files.newInputStream(path)) { settings.load(input); }
        }
        return settings;
    }

    public static void saveSettings(Path path, Properties settings) throws IOException {
        Files.createDirectories(path.getParent());
        permissions(path.getParent(), true);
        Path temporary = Files.createTempFile(path.getParent(), "account-", ".tmp");
        try {
            permissions(temporary, false);
            try (OutputStream output = Files.newOutputStream(temporary)) { settings.store(output, "Local hurricane tracker settings"); }
            try { Files.move(temporary, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
            catch (AtomicMoveNotSupportedException exception) { Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING); }
        } finally { Files.deleteIfExists(temporary); }
    }

    private static void permissions(Path path, boolean directory) throws IOException {
        try { Files.setPosixFilePermissions(path, PosixFilePermissions.fromString(directory ? "rwx------" : "rw-------")); }
        catch (UnsupportedOperationException exception) { /* Windows account permissions apply. */ }
    }
}
