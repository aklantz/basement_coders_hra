package com.controllers;

import com.hurricane.App;
import com.model.AidRequest;
import com.model.ReliefApplication;
import com.model.RequestCategory;
import com.model.User;

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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Controls the request form, review page, and submission confirmation.
 */
public class RequestReliefController {

    private static final String[] TITLES = {
        "Needs", "Household", "Location", "Contact"
    };

    private static final String[] SUBTITLES = {
        "Choose the supplies you need and how soon. * Required.",
        "Tell us who needs support and about your situation. * Required.",
        "Where can support safely reach your household? * Required.",
        "Choose how and when it is safe to reach you. * Required."
    };

    private static final String[] NEED_KEYS = {
        "water", "food", "shelter", "medical", "transport", "other"
    };

    private static final String[] NEED_LABELS = {
        "Drinking water",
        "Food",
        "Temporary shelter",
        "Medical supplies",
        "Transport",
        "Other essentials"
    };

    private static final String SELECTED_CARD =
            "-fx-background-color: #E4F3F0;"
            + "-fx-border-color: #008681;";

    @FXML private StackPane requestRoot;
    @FXML private ScrollPane wizardScroll;

    @FXML private Label stepLabel, pageTitle, pageSubtitle, draftNotice;

    @FXML private Region segment0, segment1, segment2, segment3;

    @FXML private VBox needsPage, householdPage;
    @FXML private VBox locationPage, contactPage, otherGroup;

    @FXML private VBox errorBanner;
    @FXML private Label errorHeading, errorText;
    @FXML private Label phoneError, characterCount;

    @FXML private Button continueButton;

    @FXML private CheckBox waterNeed, foodNeed, shelterNeed;
    @FXML private CheckBox medicalNeed, transportNeed, otherNeed;

    @FXML private ToggleGroup urgencyGroup, contactGroup;
    @FXML private RadioButton todayRadio, laterRadio;
    @FXML private RadioButton smsRadio, phoneRadio;

    @FXML private TextField otherField, peopleField;
    @FXML private TextField childrenField, olderField;

    @FXML private TextArea accessibilityField, situationField;
    @FXML private TextField streetField, cityField;
    @FXML private TextArea instructionsField;
    @FXML private CheckBox safeLocation;

    @FXML private TextField phoneField, safeTimeField;

    @FXML private HBox navigationBar, progressSteps, submissionBrand;
    @FXML private VBox formActions, reviewPage, submittedPage;
    @FXML private VBox reviewOtherGroup;

    @FXML private Label reviewNeeds, reviewOther;
    @FXML private Label reviewHousehold, reviewSituation;
    @FXML private Label reviewAccess, reviewLocation, reviewContact;

    @FXML private CheckBox sharingAgreement;
    @FXML private Label agreementError, reviewError;

    @FXML private Button sendButton, viewSubmittedButton;

    @FXML private Label submissionLocation, submittedReference;
    @FXML private Label submittedDate, submittedSummary, submissionNotice;

    private final Map<String, TextInputControl> fields =
            new LinkedHashMap<>();

    private final Map<TextInputControl, String> originalStyles =
            new LinkedHashMap<>();

    private UUID ownerId;
    private int step;

    private AidRequest submittedRequest;

    private boolean dirty;
    private boolean editingFromReview;

    @FXML
    private void initialize() {
        User user = ReliefApplication.getInstance().getCurrentUser();

        if (user == null || user.getUserId() == null) {
            wizardScroll.setVisible(false);
            Platform.runLater(() -> navigate("login"));
            return;
        }

        ownerId = user.getUserId();

        register("otherText", otherField);
        register("people", peopleField);
        register("children", childrenField);
        register("older", olderField);
        register("accessibility", accessibilityField);
        register("situation", situationField);
        register("street", streetField);
        register("city", cityField);
        register("instructions", instructionsField);
        register("phone", phoneField);
        register("safeTime", safeTimeField);

        situationField.setTextFormatter(
                new TextFormatter<String>(change ->
                        change.getControlNewText().length() <= 500
                                ? change
                                : null
                )
        );

        situationField.textProperty().addListener(
                (observable, oldValue, value) ->
                        characterCount.setText(
                                value.length() + " / 500 characters"
                        )
        );

        for (CheckBox check : needControls()) {
            styleChoice(check);
        }

        styleChoice(safeLocation);

        for (RadioButton radio : new RadioButton[]{
                todayRadio, laterRadio, smsRadio, phoneRadio
        }) {
            styleChoice(radio);
        }

        otherNeed.selectedProperty().addListener(
                (observable, oldValue, selected) ->
                        shown(otherGroup, selected)
        );

        phoneField.setText(value(user.getPhoneNumber()));

        sharingAgreement.selectedProperty().addListener(
                (observable, oldValue, selected) -> {
                    if (selected) {
                        shown(agreementError, false);
                    }
                }
        );

        loadDraft();

        if (step == 4) {
            int invalid = firstInvalidStep(snapshot());

            if (invalid < 0) {
                openReview();
            } else {
                showStep(invalid);
            }
        } else {
            showStep(step);
        }

        dirty = false;

        Platform.runLater(() -> requestRoot.requestFocus());
    }

    private void register(String key, TextInputControl field) {
        fields.put(key, field);
        originalStyles.put(field, field.getStyle());

        field.textProperty().addListener(
                (observable, oldValue, value) -> dirty = true
        );
    }

    private void styleChoice(
            javafx.scene.control.ToggleButton choice
    ) {
        String base = choice.getStyle();

        choice.selectedProperty().addListener(
                (observable, oldValue, selected) -> {
                    choice.setStyle(
                            base + (selected ? SELECTED_CARD : "")
                    );
                    dirty = true;
                }
        );
    }

    private void styleChoice(CheckBox choice) {
        String base = choice.getStyle();

        choice.selectedProperty().addListener(
                (observable, oldValue, selected) -> {
                    choice.setStyle(
                            base + (selected ? SELECTED_CARD : "")
                    );
                    dirty = true;
                }
        );
    }

    private CheckBox[] needControls() {
        return new CheckBox[]{
            waterNeed,
            foodNeed,
            shelterNeed,
            medicalNeed,
            transportNeed,
            otherNeed
        };
    }

    private void showStep(int value) {
        step = Math.max(0, Math.min(3, value));

        sharingAgreement.setSelected(false);

        shown(agreementError, false);
        shown(reviewPage, false);
        shown(submittedPage, false);
        shown(submissionBrand, false);

        shown(navigationBar, true);
        shown(progressSteps, true);
        shown(stepLabel, true);
        shown(formActions, true);

        VBox[] pages = {
            needsPage, householdPage, locationPage, contactPage
        };

        Region[] segments = {
            segment0, segment1, segment2, segment3
        };

        for (int index = 0; index < pages.length; index++) {
            shown(pages[index], index == step);

            segments[index].setStyle(
                    "-fx-background-color: "
                    + (index <= step ? "#008681" : "#D8E3EB")
                    + ";"
            );
        }

        stepLabel.setStyle(
                "-fx-font-size: 13px;"
                + "-fx-text-fill: #587083;"
        );

        stepLabel.setText(
                "Step " + (step + 1) + " of 4 · " + TITLES[step]
        );

        pageTitle.setText(TITLES[step]);
        pageSubtitle.setText(SUBTITLES[step]);

        continueButton.setText(
                editingFromReview
                        ? "Return to review"
                        : step == 3
                                ? "Review request"
                                : "Continue to "
                                  + TITLES[step + 1].toLowerCase(
                                          java.util.Locale.ROOT
                                  )
        );

        clearErrors();
        wizardScroll.setVvalue(0);

        Platform.runLater(() -> requestRoot.requestFocus());
    }

    @FXML
    private void next() {
        if (!ownsSession() || !validateStep(step)) {
            return;
        }

        if (editingFromReview || step == 3) {
            int invalid = firstInvalidStep(snapshot());

            if (invalid >= 0) {
                editingFromReview = false;
                showStep(invalid);
                validateStep(invalid);
                return;
            }

            openReview();
        } else {
            showStep(step + 1);
        }
    }

    @FXML
    private void back() {
        if (!ownsSession() || submittedRequest != null) {
            return;
        }

        editingFromReview = false;

        if (step == 4) {
            showStep(3);
            return;
        }

        if (step > 0) {
            showStep(step - 1);
            return;
        }

        if (!dirty) {
            navigate("my_requests");
            return;
        }

        ButtonType save = new ButtonType(
                "Save draft & exit",
                ButtonData.YES
        );

        ButtonType discard = new ButtonType(
                "Discard changes",
                ButtonData.NO
        );

        Alert alert = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Save your entries before leaving? "
                + "A draft is not a submitted request.",
                save,
                discard,
                ButtonType.CANCEL
        );

        alert.setTitle("Leave request");
        alert.setHeaderText("Your request has not been sent");

        if (requestRoot.getScene() != null) {
            alert.initOwner(requestRoot.getScene().getWindow());
        }

        alert.getDialogPane().setStyle(
                "-fx-background-color: #F4F7FA;"
                + "-fx-font-family: 'Segoe UI';"
        );

        ButtonType answer = alert.showAndWait()
                                 .orElse(ButtonType.CANCEL);

        if (answer == save) {
            saveDraftAndExit();
        }

        if (answer == discard) {
            navigate("my_requests");
        }
    }

    private boolean validateStep(int page) {
        clearErrors();

        Map<String, String> errors =
                validateValues(snapshot(), page);

        if (errors.isEmpty()) {
            return true;
        }

        errorHeading.setText(
                "Check " + errors.size() + " "
                + (errors.size() == 1 ? "field" : "fields")
                + " before continuing"
        );

        errorText.setText(
                String.join("\n", errors.values())
                + "\nYour other details are unchanged."
        );

        shown(errorBanner, true);

        for (String key : errors.keySet()) {
            TextInputControl field = fields.get(key);

            if (field != null) {
                field.setStyle(
                        originalStyles.get(field)
                        + "-fx-border-color: #D43D47;"
                );
            }
        }

        shown(phoneError, errors.containsKey("phone"));
        wizardScroll.setVvalue(0);

        return false;
    }

    private void clearErrors() {
        shown(errorBanner, false);
        shown(phoneError, false);

        for (Map.Entry<TextInputControl, String> entry
                : originalStyles.entrySet()) {
            entry.getKey().setStyle(entry.getValue());
        }
    }

    /**
     * Validates a form step without changing the UI.
     */
    static Map<String, String> validateValues(
            Properties data,
            int page
    ) {
        Map<String, String> errors = new LinkedHashMap<>();

        if (page == 0) {
            boolean any = false;

            for (String key : NEED_KEYS) {
                any |= selected(data, key);
            }

            if (!any) {
                errors.put(
                        "needs",
                        "Choose at least one type of support."
                );
            }

            if (selected(data, "other")
                    && blank(data, "otherText")) {
                errors.put(
                        "otherText",
                        "Describe the other essentials you need."
                );
            }

            String urgency = data.getProperty("urgency", "");

            if (!urgency.equals("today")
                    && !urgency.equals("later")) {
                errors.put(
                        "urgency",
                        "Choose how soon support is needed."
                );
            }

        } else if (page == 1) {
            int people = number(data, "people");
            int children = number(data, "children");
            int older = number(data, "older");

            if (people < 1) {
                errors.put(
                        "people",
                        "Enter a positive whole number of people."
                );
            }

            if (children < 0) {
                errors.put(
                        "children",
                        "Enter zero or more children as a whole number."
                );
            }

            if (older < 0) {
                errors.put(
                        "older",
                        "Enter zero or more older adults as a whole number."
                );
            }

            if (people > 0
                    && children >= 0
                    && older >= 0
                    && (long) children + older > people) {
                errors.put(
                        "children",
                        "Children and older adults together "
                        + "cannot exceed the total people."
                );
            }

            if (blank(data, "situation")
                    || data.getProperty("situation", "").length() > 500) {
                errors.put(
                        "situation",
                        "Describe your situation using 1–500 characters."
                );
            }

        } else if (page == 2) {
            if (blank(data, "street")) {
                errors.put("street", "Enter a street address.");
            }

            if (blank(data, "city")) {
                errors.put("city", "Enter the city, state and ZIP.");
            }

            if (blank(data, "instructions")) {
                errors.put(
                        "instructions",
                        "Add access or safe pickup instructions."
                );
            }

            if (!selected(data, "safe")) {
                errors.put(
                        "safe",
                        "Confirm you can safely meet at this location, "
                        + "or choose a safer location."
                );
            }

        } else if (page == 3) {
            String contact = data.getProperty("contact", "");

            if (!contact.equals("sms")
                    && !contact.equals("phone")) {
                errors.put(
                        "contact",
                        "Choose a preferred contact method."
                );
            }

            if (normalizedPhone(data.getProperty("phone", "")) == null) {
                errors.put(
                        "phone",
                        "Contact phone number is incomplete or invalid. "
                        + "Enter a 10-digit US number, including area code."
                );
            }

            if (blank(data, "safeTime")) {
                errors.put(
                        "safeTime",
                        "Tell us when it is safe to contact you."
                );
            }
        }

        return errors;
    }

    static String normalizedPhone(String value) {
        String input = value == null ? "" : value.trim();

        if (!input.matches("\\+?[0-9() .-]+")) {
            return null;
        }

        String digits = input.replaceAll("[^0-9]", "");

        if (input.startsWith("+")
                && (digits.length() != 11
                    || !digits.startsWith("1"))) {
            return null;
        }

        if (digits.length() == 11 && digits.startsWith("1")) {
            digits = digits.substring(1);
        }

        return digits.length() == 10 ? digits : null;
    }

    private static int number(Properties data, String key) {
        try {
            return Integer.parseInt(
                    data.getProperty(key, "").trim()
            );
        } catch (NumberFormatException exception) {
            return -1;
        }
    }

    private static boolean blank(
            Properties data,
            String key
    ) {
        return data.getProperty(key, "").trim().isEmpty();
    }

    private static boolean selected(
            Properties data,
            String key
    ) {
        return Boolean.parseBoolean(
                data.getProperty(key, "false")
        );
    }

    private Properties snapshot() {
        Properties data = new Properties();

        data.setProperty("step", String.valueOf(step));

        data.setProperty(
                "sharingAgreed",
                String.valueOf(sharingAgreement.isSelected())
        );

        for (Map.Entry<String, TextInputControl> entry
                : fields.entrySet()) {
            data.setProperty(
                    entry.getKey(),
                    entry.getValue().getText().trim()
            );
        }

        CheckBox[] checks = needControls();

        for (int index = 0; index < checks.length; index++) {
            data.setProperty(
                    NEED_KEYS[index],
                    String.valueOf(checks[index].isSelected())
            );
        }

        data.setProperty(
                "safe",
                String.valueOf(safeLocation.isSelected())
        );

        data.setProperty(
                "urgency",
                todayRadio.isSelected()
                        ? "today"
                        : laterRadio.isSelected() ? "later" : ""
        );

        data.setProperty(
                "contact",
                smsRadio.isSelected()
                        ? "sms"
                        : phoneRadio.isSelected() ? "phone" : ""
        );

        return data;
    }

    private static String needsText(Properties data) {
        List<String> needs = new ArrayList<>();

        for (int index = 0; index < NEED_KEYS.length; index++) {
            if (selected(data, NEED_KEYS[index])) {
                needs.add(NEED_LABELS[index]);
            }
        }

        return String.join(", ", needs);
    }

    static RequestCategory primaryCategory(Properties data) {
        RequestCategory[] categories = {
            RequestCategory.WATER,
            RequestCategory.FOOD,
            RequestCategory.OTHER,
            RequestCategory.MEDICAL,
            RequestCategory.TRANSPORT,
            RequestCategory.SUPPLIES
        };

        RequestCategory result = null;
        int count = 0;

        for (int index = 0; index < NEED_KEYS.length; index++) {
            if (selected(data, NEED_KEYS[index])) {
                result = categories[index];
                count++;
            }
        }

        return count > 1 ? RequestCategory.OTHER : result;
    }

    static String requestNote(Properties data) {
        return "Requested needs: " + needsText(data)
                + (selected(data, "other")
                        ? "\nOther essentials: "
                          + data.getProperty("otherText", "")
                        : "")
                + "\nSupport needed: "
                + (data.getProperty("urgency", "").equals("today")
                        ? "Today"
                        : "Within the next few days")
                + "\nChildren under 18: "
                + data.getProperty("children", "0")
                + "\nAdults 65 or older: "
                + data.getProperty("older", "0")
                + "\nAccessibility / medical supplies: "
                + data.getProperty("accessibility", "")
                + "\nSituation: "
                + data.getProperty("situation", "")
                + "\nSafe pickup instructions: "
                + data.getProperty("instructions", "")
                + "\nCan safely meet at location: "
                + selected(data, "safe")
                + "\nPreferred contact: "
                + (data.getProperty("contact", "").equals("sms")
                        ? "Text message (SMS)"
                        : "Phone call")
                + "\nContact phone: "
                + normalizedPhone(data.getProperty("phone", ""))
                + "\nSafe contact times / instructions: "
                + data.getProperty("safeTime", "")
                + "\nDetails sharing agreement: "
                + (selected(data, "sharingAgreed")
                        ? "Accepted"
                        : "Not accepted");
    }

    static int firstInvalidStep(Properties data) {
        for (int page = 0; page < 4; page++) {
            if (!validateValues(data, page).isEmpty()) {
                return page;
            }
        }

        return -1;
    }

    static boolean canSubmit(Properties data) {
        return firstInvalidStep(data) < 0
                && selected(data, "sharingAgreed");
    }

    private void showOnly(VBox target) {
        for (VBox page : new VBox[]{
                needsPage,
                householdPage,
                locationPage,
                contactPage,
                reviewPage,
                submittedPage
        }) {
            shown(page, page == target);
        }

        shown(formActions, false);
        shown(progressSteps, false);
        shown(draftNotice, false);

        shown(submissionBrand, target == submittedPage);

        clearErrors();
        wizardScroll.setVvalue(0);

        Platform.runLater(() -> requestRoot.requestFocus());
    }

    private void openReview() {
        Properties data = snapshot();

        step = 4;
        editingFromReview = false;

        showOnly(reviewPage);

        shown(navigationBar, true);
        shown(stepLabel, true);

        stepLabel.setStyle(
                "-fx-font-size: 12px;"
                + "-fx-font-weight: bold;"
                + "-fx-text-fill: #008681;"
        );

        stepLabel.setText("Review & submit");

        pageTitle.setText("Review your request");

        pageSubtitle.setText(
                "Check that your details are correct before sharing."
        );

        sharingAgreement.setSelected(false);

        shown(agreementError, false);
        shown(reviewError, false);

        sendButton.setDisable(false);

        reviewNeeds.setText(
                needsText(data)
                + "\nNeeded "
                + (todayRadio.isSelected()
                        ? "today"
                        : "within the next few days")
                + " · Non-emergency"
        );

        shown(reviewOtherGroup, selected(data, "other"));

        reviewOther.setText(
                data.getProperty("otherText", "")
        );

        reviewHousehold.setText(
                data.getProperty("people") + " people · "
                + data.getProperty("children") + " children under 18 · "
                + data.getProperty("older") + " adults 65 or older"
        );

        reviewSituation.setText(
                data.getProperty("situation")
        );

        reviewAccess.setText(
                blank(data, "accessibility")
                        ? "None provided"
                        : data.getProperty("accessibility")
        );

        reviewLocation.setText(
                data.getProperty("street")
                + ", " + data.getProperty("city")
                + "\n" + data.getProperty("instructions")
        );

        reviewContact.setText(
                (smsRadio.isSelected()
                        ? "Text message (SMS)"
                        : "Phone call")
                + " · " + data.getProperty("phone")
                + "\n" + data.getProperty("safeTime")
        );
    }

    private void editStep(int page, String fieldKey) {
        if (!ownsSession() || submittedRequest != null) {
            return;
        }

        editingFromReview = true;
        showStep(page);

        Platform.runLater(() -> {
            TextInputControl field = fields.get(fieldKey);

            if (field != null) {
                field.requestFocus();
            }
        });
    }

    @FXML
    private void editNeeds() {
        editStep(0, null);
    }

    @FXML
    private void editOther() {
        editStep(0, "otherText");
    }

    @FXML
    private void editHousehold() {
        editStep(1, "people");
    }

    @FXML
    private void editSituation() {
        editStep(1, "situation");
    }

    @FXML
    private void editAccess() {
        editStep(1, "accessibility");
    }

    @FXML
    private void editLocation() {
        editStep(2, "street");
    }

    @FXML
    private void editContact() {
        editStep(3, "phone");
    }

    @FXML
    private void sendRequest() {
        if (!ownsSession()
                || submittedRequest != null
                || step != 4) {
            return;
        }

        Properties data = snapshot();

        int invalid = firstInvalidStep(data);

        if (invalid >= 0) {
            showStep(invalid);
            validateStep(invalid);
            return;
        }

        if (!canSubmit(data)) {
            shown(agreementError, true);
            sharingAgreement.requestFocus();
            return;
        }

        sendButton.setDisable(true);

        AidRequest created;

        try {
            created = ReliefApplication.getInstance().createAidRequest(
                    primaryCategory(data),
                    number(data, "people"),
                    data.getProperty("street")
                            + ", " + data.getProperty("city"),
                    requestNote(data)
            );
        } catch (RuntimeException exception) {
            created = null;
        }

        if (created == null) {
            reviewError.setText(
                    "Your request could not be saved. "
                    + "Your entries are still here. Please try again."
            );

            shown(reviewError, true);
            sendButton.setDisable(false);
            return;
        }

        submittedRequest = created;
        dirty = false;

        String cleanupMessage = "";

        try {
            deleteDraft(draftPath(ownerId));
        } catch (IOException | SecurityException exception) {
            cleanupMessage =
                    "Your request was submitted, but this device "
                    + "could not remove its saved draft. "
                    + "Do not submit that draft again.";
        }

        showSubmitted(data, cleanupMessage);
    }

    private void showSubmitted(
            Properties data,
            String cleanupMessage
    ) {
        step = 5;

        showOnly(submittedPage);

        shown(navigationBar, false);
        shown(stepLabel, false);

        pageTitle.setText("Request submitted");

        pageSubtitle.setText(
                "Your relief request was successfully submitted. "
                + "Keep this reference for your records."
        );

        submissionLocation.setText(
                data.getProperty("city", "")
        );

        submittedReference.setText(
                submittedRequest.getRequestId()
                                .toString()
                                .toUpperCase(java.util.Locale.ROOT)
        );

        java.time.LocalDateTime createdAt =
                submittedRequest.getCreatedAt();

        submittedDate.setText(
                createdAt == null
                        ? "Submission time unavailable"
                        : "Submitted "
                          + java.time.format.DateTimeFormatter.ofPattern(
                                  "MMM d, yyyy · h:mm a",
                                  java.util.Locale.US
                          ).format(createdAt)
        );

        submittedSummary.setText(
                needsText(data)
                + (selected(data, "other")
                        ? " · " + data.getProperty("otherText")
                        : "")
                + "\nNeeded "
                + (data.getProperty("urgency").equals("today")
                        ? "today"
                        : "within the next few days")
                + " · Non-emergency"
        );

        submissionNotice.setText(cleanupMessage);

        shown(
                submissionNotice,
                !cleanupMessage.isEmpty()
        );
    }

    @FXML
    private void viewSubmittedRequest() {
        if (!ownsSession() || submittedRequest == null) {
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    App.class.getResource("my_requests.fxml")
            );

            Parent view = loader.load();

            MyRequestsController controller =
                    loader.getController();

            requestRoot.getScene().setRoot(view);

            controller.openRequest(
                    submittedRequest.getRequestId()
            );

        } catch (IOException exception) {
            showMessage(
                    "Unable to open request",
                    "Your request is saved. "
                    + "The request details screen could not be opened."
            );
        }
    }

    @FXML
    private void openHome() {
        if (ownsSession()) {
            navigate("user_home");
        }
    }

    @FXML
    private void saveDraftAndExit() {
        if (!ownsSession() || submittedRequest != null) {
            return;
        }

        try {
            saveDraft(draftPath(ownerId), snapshot());
            dirty = false;
        } catch (IOException | SecurityException exception) {
            showMessage(
                    "Draft not saved",
                    "We could not save this draft on this device. "
                    + "Your entries are still here. Please try again."
            );
            return;
        }

        navigate("my_requests");
    }

    private void loadDraft() {
        try {
            Path path = draftPath(ownerId);

            if (!Files.exists(path)) {
                return;
            }

            Properties data = readDraft(path);

            for (Map.Entry<String, TextInputControl> entry
                    : fields.entrySet()) {
                entry.getValue().setText(
                        data.getProperty(
                                entry.getKey(),
                                entry.getValue().getText()
                        )
                );
            }

            CheckBox[] checks = needControls();

            for (int index = 0; index < checks.length; index++) {
                checks[index].setSelected(
                        selected(data, NEED_KEYS[index])
                );
            }

            safeLocation.setSelected(
                    selected(data, "safe")
            );

            todayRadio.setSelected(
                    data.getProperty("urgency", "").equals("today")
            );

            laterRadio.setSelected(
                    data.getProperty("urgency", "").equals("later")
            );

            smsRadio.setSelected(
                    data.getProperty("contact", "").equals("sms")
            );

            phoneRadio.setSelected(
                    data.getProperty("contact", "").equals("phone")
            );

            step = Math.max(
                    0,
                    Math.min(4, number(data, "step"))
            );

            draftNotice.setText(
                    "Saved draft restored · "
                    + "Your request has not been sent."
            );

            shown(draftNotice, true);

        } catch (IOException
                | IllegalArgumentException
                | SecurityException exception) {
            draftNotice.setText(
                    "Your saved draft could not be opened. "
                    + "It has not been deleted. "
                    + "You can start a new request."
            );

            shown(draftNotice, true);
        }
    }

    private static Path draftPath(UUID userId) {
        return Paths.get(
                System.getProperty("user.home"),
                ".hurricane-tracker",
                "request-drafts",
                userId + ".properties"
        );
    }

    static Properties readDraft(Path path) throws IOException {
        Properties data = new Properties();

        try (InputStream input = Files.newInputStream(path)) {
            data.load(input);
        }

        return data;
    }

    static void saveDraft(
            Path path,
            Properties data
    ) throws IOException {
        Files.createDirectories(path.getParent());

        privatePermissions(path.getParent(), true);

        Path temporary = Files.createTempFile(
                path.getParent(),
                "request-",
                ".tmp"
        );

        try {
            privatePermissions(temporary, false);

            try (OutputStream output =
                    Files.newOutputStream(temporary)) {
                data.store(
                        output,
                        "Unsubmitted hurricane relief request draft"
                );
            }

            try {
                Files.move(
                        temporary,
                        path,
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING
                );
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(
                        temporary,
                        path,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }

        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    static void deleteDraft(Path path) throws IOException {
        Files.deleteIfExists(path);
    }

    private static void privatePermissions(
            Path path,
            boolean directory
    ) throws IOException {
        try {
            Files.setPosixFilePermissions(
                    path,
                    PosixFilePermissions.fromString(
                            directory ? "rwx------" : "rw-------"
                    )
            );
        } catch (UnsupportedOperationException exception) {
            // Windows uses the current account's file permissions.
        }
    }

    private boolean ownsSession() {
        User user = ReliefApplication.getInstance().getCurrentUser();

        if (user != null
                && ownerId != null
                && ownerId.equals(user.getUserId())) {
            return true;
        }

        wizardScroll.setVisible(false);
        navigate("login");

        return false;
    }

    private void navigate(String screen) {
        try {
            App.setRoot(screen);
        } catch (IOException exception) {
            showMessage(
                    "Unable to open screen",
                    "The " + screen + " screen could not be opened. "
                    + "Your saved request or draft has not been changed."
            );
        }
    }

    private void showMessage(String title, String text) {
        clearErrors();

        errorHeading.setText(title);
        errorText.setText(text);

        shown(errorBanner, true);
        wizardScroll.setVvalue(0);
    }

    private static void shown(Node node, boolean visible) {
        node.setVisible(visible);
        node.setManaged(visible);
    }

    private static String value(String value) {
        return value == null ? "" : value;
    }

}