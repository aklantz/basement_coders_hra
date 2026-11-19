package com.controllers;

import com.hurricane.App;
import com.model.AidRequest;
import com.model.ReliefApplication;
import com.model.RequestCategory;
import com.model.RequestStatus;
import com.model.ResidentAccount;
import com.model.ResidentRequests;
import com.model.TaskAssignment;
import com.model.User;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.UUID;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

/**
 * Controls request details, editing, and cancellation.
 */
public class RequestDetailController {

    @FXML private StackPane detailRoot;
    @FXML private StackPane cancelOverlay;
    @FXML private BorderPane mainContent;
    @FXML private ScrollPane pageScroll;

    @FXML private Label locationLabel;
    @FXML private Label pageTitle;
    @FXML private Label referenceLabel;
    @FXML private Label statusLabel;
    @FXML private Label messageLabel;

    @FXML private VBox detailPage;
    @FXML private VBox editPage;

    @FXML private Button editRequestButton;
    @FXML private Button cancelRequestButton;

    @FXML private Label progressLabel;
    @FXML private Label updateTitle;
    @FXML private Label updateLabel;

    @FXML private Label needsLabel;
    @FXML private Label householdLabel;
    @FXML private Label handoffLabel;
    @FXML private Label contactLabel;
    @FXML private Label originalNoteLabel;

    @FXML private Label editNeedsSummary;
    @FXML private Label editHouseholdSummary;
    @FXML private Label editLocationSummary;
    @FXML private Label editContactSummary;
    @FXML private Label editSituationSummary;

    @FXML private VBox needsForm;
    @FXML private VBox householdForm;
    @FXML private VBox locationForm;
    @FXML private VBox contactForm;
    @FXML private VBox situationForm;

    @FXML private ComboBox<RequestCategory> categoryField;
    @FXML private ComboBox<String> urgencyField;
    @FXML private ComboBox<String> contactMethodField;

    @FXML private TextField needsField;
    @FXML private TextField otherField;
    @FXML private TextField peopleField;
    @FXML private TextField childrenField;
    @FXML private TextField olderField;

    @FXML private TextField addressField;
    @FXML private TextField phoneField;
    @FXML private TextField safeTimeField;

    @FXML private TextArea instructionsField;
    @FXML private TextArea situationField;
    @FXML private TextArea accessibilityField;

    @FXML private Label cancelSummary;
    @FXML private Label cancelError;

    @FXML private Button keepRequestButton;
    @FXML private Button confirmCancelButton;

    private UUID ownerId;
    private UUID selectedId;

    private AidRequest selected;
    private Properties baseDetails;

    private String editVersion;
    private String cancelVersion;

    private boolean editing;
    private boolean dirty;

    @FXML
    private void initialize() {
        User user = currentUser();

        if (user == null) {
            mainContent.setVisible(false);
            Platform.runLater(() -> navigate("login"));
            return;
        }

        ownerId = user.getUserId();

        for (RequestCategory category : RequestCategory.values()) {
            if (category != RequestCategory.RESCUE) {
                categoryField.getItems().add(category);
            }
        }

        categoryField.setConverter(
                new StringConverter<RequestCategory>() {
                    @Override
                    public String toString(RequestCategory value) {
                        return value == null
                                ? ""
                                : categoryName(value);
                    }

                    @Override
                    public RequestCategory fromString(String value) {
                        throw new UnsupportedOperationException();
                    }
                }
        );

        urgencyField.getItems().setAll(
                "Not recorded",
                "Today",
                "Within the next few days"
        );

        contactMethodField.getItems().setAll(
                "Not recorded",
                "Text message (SMS)",
                "Phone call"
        );

        TextInputControl[] inputs = {
            needsField,
            otherField,
            peopleField,
            childrenField,
            olderField,
            addressField,
            phoneField,
            safeTimeField,
            instructionsField,
            situationField,
            accessibilityField
        };

        for (TextInputControl field : inputs) {
            field.textProperty().addListener(
                    (observable, oldValue, value) -> {
                        if (editing) {
                            dirty = true;
                            updateEditSummaries();
                        }
                    }
            );
        }

        categoryField.valueProperty().addListener(
                (observable, oldValue, value) -> {
                    if (editing) {
                        dirty = true;
                    }
                }
        );

        urgencyField.valueProperty().addListener(
                (observable, oldValue, value) -> {
                    if (editing) {
                        dirty = true;
                        updateEditSummaries();
                    }
                }
        );

        contactMethodField.valueProperty().addListener(
                (observable, oldValue, value) -> {
                    if (editing) {
                        dirty = true;
                        updateEditSummaries();
                    }
                }
        );

        editRequestButton.setDisable(true);
        cancelRequestButton.setDisable(true);
    }

    /**
     * Opens a request belonging to the signed-in user.
     *
     * @param id the request ID
     */
    public void openRequest(UUID id) {
        selectedId = id;

        if (!resolve()) {
            return;
        }

        renderDetails();
    }

    private boolean session() {
        User user = currentUser();

        if (user != null
                && ownerId != null
                && ownerId.equals(user.getUserId())) {
            return true;
        }

        mainContent.setVisible(false);
        shown(cancelOverlay, false);

        editing = false;
        dirty = false;

        navigate("login");
        return false;
    }

    private boolean resolve() {
        if (!session()) {
            return false;
        }

        selected = null;

        List<AidRequest> requests =
                ReliefApplication.getInstance().getRequests();

        if (requests != null && selectedId != null) {
            for (AidRequest request : requests) {
                if (ResidentRequests.belongsTo(request, currentUser())
                        && selectedId.equals(request.getRequestId())) {
                    selected = request;
                    return true;
                }
            }
        }

        shown(detailPage, false);
        shown(editPage, false);

        pageTitle.setText("Request unavailable");
        referenceLabel.setText("Open a request from My requests.");

        editRequestButton.setDisable(true);
        cancelRequestButton.setDisable(true);

        message("This request could not be found in your account.");

        return false;
    }

    private void renderDetails() {
        editing = false;
        dirty = false;

        shown(detailPage, true);
        shown(editPage, false);
        shown(messageLabel, false);

        Properties data = ResidentRequests.details(selected);

        boolean changeable =
                ResidentRequests.canChange(selected, currentUser());

        editRequestButton.setDisable(!changeable);
        cancelRequestButton.setDisable(!changeable);

        pageTitle.setText(
                ResidentRequests.isCancelled(selected)
                        ? "Request cancelled"
                        : "Request received"
        );

        referenceLabel.setText(
                selected.getRequestId()
                + " · Submitted "
                + date(selected)
        );

        statusLabel.setText(status(selected));

        locationLabel.setText(
                value(
                        selected.getStreetAddress(),
                        "Location not recorded"
                )
        );

        needsLabel.setText(needSummary(data));
        householdLabel.setText(householdSummary(data));

        handoffLabel.setText(
                selected.getStreetAddress()
                + "\n"
                + value(
                        data.getProperty("instructions"),
                        "Pickup instructions not recorded"
                )
        );

        contactLabel.setText(contactSummary(data));

        String original = data.getProperty("originalNote", "");

        originalNoteLabel.setText(
                original.isEmpty()
                        ? ""
                        : "Original saved note\n" + original
        );

        shown(originalNoteLabel, !original.isEmpty());

        progressLabel.setText(progress(selected));

        updateTitle.setText(
                changeable
                        ? "No coordinator assigned yet"
                        : "Current request status"
        );

        String coordinator = coordinatorName(selected);

        String update;

        if (changeable) {
            update = "Updates will appear here. Review times and "
                    + "assistance are not guaranteed.";
        } else if (ResidentRequests.isCancelled(selected)) {
            update = "You cancelled this request. "
                    + "It is no longer awaiting review.";
        } else {
            update = "The recorded status is "
                    + status(selected).toLowerCase(Locale.ROOT)
                    + ". Only unassigned requests awaiting review "
                    + "can be edited or cancelled here.";
        }

        updateLabel.setText(
                (coordinator.isEmpty()
                        ? ""
                        : "Coordinator: " + coordinator + "\n")
                + update
        );

        pageScroll.setVvalue(0);
    }

    @FXML
    private void beginEdit() {
        beginEdit(null);
    }

    private void beginEdit(VBox section) {
        if (!resolve()) {
            return;
        }

        if (!ResidentRequests.canChange(selected, currentUser())) {
            renderDetails();

            message(
                    "This request is no longer available for editing."
            );
            return;
        }

        baseDetails = ResidentRequests.details(selected);
        editVersion = ResidentRequests.version(selected);

        editing = false;

        categoryField.setValue(selected.getCategory());

        needsField.setText(
                baseDetails.getProperty("needs", "")
        );

        otherField.setText(
                baseDetails.getProperty("otherText", "")
        );

        peopleField.setText(
                String.valueOf(selected.getHouseholdSize())
        );

        childrenField.setText(
                baseDetails.getProperty("children", "")
        );

        olderField.setText(
                baseDetails.getProperty("older", "")
        );

        addressField.setText(
                value(selected.getStreetAddress(), "")
        );

        instructionsField.setText(
                baseDetails.getProperty("instructions", "")
        );

        phoneField.setText(
                baseDetails.getProperty("phone", "")
        );

        safeTimeField.setText(
                baseDetails.getProperty("safeTime", "")
        );

        situationField.setText(
                baseDetails.getProperty("situation", "")
        );

        accessibilityField.setText(
                baseDetails.getProperty("accessibility", "")
        );

        String urgency = baseDetails.getProperty(
                "urgencyText",
                "Not recorded"
        );

        urgencyField.setValue(
                urgencyField.getItems().contains(urgency)
                        ? urgency
                        : "Not recorded"
        );

        String contact = baseDetails.getProperty(
                "contactText",
                "Not recorded"
        );

        contactMethodField.setValue(
                contactMethodField.getItems().contains(contact)
                        ? contact
                        : "Not recorded"
        );

        for (VBox form : new VBox[]{
                needsForm,
                householdForm,
                locationForm,
                contactForm,
                situationForm
        }) {
            shown(form, false);
        }

        shown(detailPage, false);
        shown(editPage, true);
        shown(messageLabel, false);

        pageTitle.setText("Edit request");

        referenceLabel.setText(
                selected.getRequestId()
                + " · Update only the sections you need."
        );

        editing = true;
        dirty = false;

        updateEditSummaries();

        if (section != null) {
            shown(section, true);
        }

        pageScroll.setVvalue(0);
    }

    @FXML
    private void editNeeds() {
        beginEdit(needsForm);
    }

    @FXML
    private void editHousehold() {
        beginEdit(householdForm);
    }

    @FXML
    private void editLocation() {
        beginEdit(locationForm);
    }

    @FXML
    private void editContact() {
        beginEdit(contactForm);
    }

    @FXML
    private void toggleNeeds() {
        shown(needsForm, !needsForm.isVisible());
    }

    @FXML
    private void toggleHousehold() {
        shown(householdForm, !householdForm.isVisible());
    }

    @FXML
    private void toggleLocation() {
        shown(locationForm, !locationForm.isVisible());
    }

    @FXML
    private void toggleContact() {
        shown(contactForm, !contactForm.isVisible());
    }

    @FXML
    private void toggleSituation() {
        shown(situationForm, !situationForm.isVisible());
    }

    private Properties editedDetails() {
        Properties data = new Properties();
        data.putAll(baseDetails);

        String[][] values = {
            {"needs", needsField.getText()},
            {"otherText", otherField.getText()},
            {"people", peopleField.getText()},
            {"children", childrenField.getText()},
            {"older", olderField.getText()},
            {"address", addressField.getText()},
            {"instructions", instructionsField.getText()},
            {"phone", phoneField.getText()},
            {"safeTime", safeTimeField.getText()},
            {"situation", situationField.getText()},
            {"accessibility", accessibilityField.getText()}
        };

        for (String[] item : values) {
            data.setProperty(item[0], item[1].trim());
        }

        data.setProperty(
                "other",
                String.valueOf(!otherField.getText().trim().isEmpty())
        );

        data.setProperty(
                "urgencyText",
                value(urgencyField.getValue(), "Not recorded")
        );

        data.setProperty(
                "contactText",
                value(contactMethodField.getValue(), "Not recorded")
        );

        return data;
    }

    private void updateEditSummaries() {
        if (baseDetails == null) {
            return;
        }

        Properties data = editedDetails();

        editNeedsSummary.setText(needSummary(data));
        editHouseholdSummary.setText(householdSummary(data));

        editLocationSummary.setText(
                data.getProperty("address")
                + "\n"
                + data.getProperty("instructions")
        );

        editContactSummary.setText(contactSummary(data));

        editSituationSummary.setText(
                value(
                        data.getProperty("situation"),
                        "Situation not recorded"
                )
                + "\n"
                + value(
                        data.getProperty("accessibility"),
                        "Access needs not recorded"
                )
        );
    }

    @FXML
    private void saveChanges() {
        if (!editing || !resolve()) {
            return;
        }

        Properties data = editedDetails();
        List<String> errors = new ArrayList<>();

        int people = integer(data.getProperty("people"));

        int children = data.getProperty("children").isEmpty()
                ? 0
                : integer(data.getProperty("children"));

        int older = data.getProperty("older").isEmpty()
                ? 0
                : integer(data.getProperty("older"));

        if (people < 1) {
            errors.add(
                    "Enter a positive whole number of people."
            );
        }

        if (children < 0
                || older < 0
                || (people > 0 && (long) children + older > people)) {
            errors.add(
                    "Check the age counts against the household total."
            );
        }

        if (categoryField.getValue() == null
                || data.getProperty("needs").isEmpty()) {
            errors.add(
                    "Choose a category and describe the needs."
            );
        }

        if (data.getProperty("address").isEmpty()) {
            errors.add("Enter a safe handoff address.");
        }

        if (!data.getProperty("phone").isEmpty()
                && ResidentAccount.phoneDigits(
                        data.getProperty("phone")
                ) == null) {
            errors.add(
                    "Enter a 10-digit US safe contact number, "
                    + "or leave an unknown number blank."
            );
        }

        if (data.getProperty("situation").length() > 500) {
            errors.add(
                    "Keep the situation to 500 characters."
            );
        }

        if (!errors.isEmpty()) {
            message(String.join("\n", errors));
            return;
        }

        data.setProperty(
                "lastEditedAt",
                java.time.LocalDateTime.now().toString()
        );

        boolean saved =
                ReliefApplication.getInstance().editMyRequest(
                        selectedId,
                        editVersion,
                        categoryField.getValue(),
                        people,
                        data.getProperty("address"),
                        ResidentRequests.encodeDetails(data)
                );

        if (!saved) {
            message(
                    "Changes were not saved. "
                    + "The request may have changed status, "
                    + "or saving failed. Your edits are still here; "
                    + "go Back to reload the saved request."
            );
            return;
        }

        resolve();
        renderDetails();
        message("Changes saved.");
    }

    @FXML
    private void discardChanges() {
        if (!editing || !dirty || confirmDiscard()) {
            if (resolve()) {
                renderDetails();
            }
        }
    }

    private boolean confirmDiscard() {
        Alert alert = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Discard your unsaved changes?",
                ButtonType.YES,
                ButtonType.CANCEL
        );

        alert.setTitle("Discard changes");

        alert.setHeaderText(
                "The saved request will stay unchanged."
        );

        if (detailRoot.getScene() != null) {
            alert.initOwner(detailRoot.getScene().getWindow());
        }

        return alert.showAndWait().orElse(ButtonType.CANCEL)
                == ButtonType.YES;
    }

    @FXML
    private void back() {
        if (editing) {
            discardChanges();
        } else {
            navigate("my_requests");
        }
    }

    @FXML
    private void askCancel() {
        if (!resolve()) {
            return;
        }

        if (!ResidentRequests.canChange(selected, currentUser())) {
            renderDetails();

            message(
                    "This request can no longer be cancelled here."
            );
            return;
        }

        cancelVersion = ResidentRequests.version(selected);

        cancelSummary.setText(
                selected.getRequestId()
                + "\n"
                + needSummary(ResidentRequests.details(selected))
                + "\nAwaiting review · No assistance assigned"
        );

        shown(cancelError, false);
        confirmCancelButton.setDisable(false);

        mainContent.setDisable(true);

        shown(cancelOverlay, true);
        keepRequestButton.requestFocus();
    }

    @FXML
    private void keepRequest() {
        shown(cancelOverlay, false);
        mainContent.setDisable(false);

        cancelRequestButton.requestFocus();
    }

    @FXML
    private void cancelKey(KeyEvent event) {
        if (event.getCode() == KeyCode.ESCAPE) {
            keepRequest();
            event.consume();
        }
    }

    @FXML
    private void confirmCancel() {
        if (!session()) {
            return;
        }

        confirmCancelButton.setDisable(true);

        boolean saved =
                ReliefApplication.getInstance().cancelMyRequest(
                        selectedId,
                        cancelVersion
                );

        if (!saved) {
            cancelError.setText(
                    "Cancellation was not saved. "
                    + "The request may have changed, or saving failed. "
                    + "Keep the request and reload its status "
                    + "before trying again."
            );

            shown(cancelError, true);
            confirmCancelButton.setDisable(false);
            return;
        }

        keepRequest();

        if (resolve()) {
            renderDetails();
            message("Your request has been cancelled.");
        }
    }

    private static String needSummary(Properties data) {
        return value(
                data.getProperty("needs"),
                "Needs not recorded"
        ) + "\n" + value(
                data.getProperty("urgencyText"),
                "Urgency not recorded"
        );
    }

    private static String householdSummary(Properties data) {
        return data.getProperty("people", "?")
                + " people"
                + (data.getProperty("children", "").isEmpty()
                        ? ""
                        : " · " + data.getProperty("children")
                          + " children")
                + (data.getProperty("older", "").isEmpty()
                        ? ""
                        : " · " + data.getProperty("older")
                          + " adults 65+")
                + "\n"
                + value(
                        data.getProperty("accessibility"),
                        "Access needs not recorded"
                );
    }

    private static String contactSummary(Properties data) {
        return value(
                data.getProperty("contactText"),
                "Method not recorded"
        ) + " · " + value(
                data.getProperty("phone"),
                "Phone not recorded"
        ) + "\n" + value(
                data.getProperty("safeTime"),
                "Contact times not recorded"
        );
    }

    private static String date(AidRequest request) {
        return request.getCreatedAt() == null
                ? "date not recorded"
                : DateTimeFormatter.ofPattern(
                        "MMM d, yyyy · h:mm a",
                        Locale.US
                ).format(request.getCreatedAt());
    }

    private static String categoryName(
            RequestCategory category
    ) {
        return category.name()
                       .toLowerCase(Locale.ROOT)
                       .replace('_', ' ');
    }

    public static String status(AidRequest request) {
        if (ResidentRequests.isCancelled(request)) {
            return "CANCELLED";
        }

        if (request.getStatus() == null) {
            return "STATUS UNAVAILABLE";
        }

        switch (request.getStatus()) {
            case SUBMITTED:
                return "AWAITING REVIEW";

            case FULFILLED:
                return "COMPLETED";

            default:
                return request.getStatus()
                              .name()
                              .replace('_', ' ');
        }
    }

    private static String progress(AidRequest request) {
        String submitted = "Submitted · " + date(request);

        if (ResidentRequests.isCancelled(request)) {
            return submitted
                    + "\nCancelled by requester · "
                    + "No longer awaiting review";
        }

        RequestStatus state = request.getStatus();

        if (state == RequestStatus.SUBMITTED) {
            return submitted
                    + "\n● Awaiting coordinator review"
                    + "\n○ Assistance assignment"
                    + "\n○ Completion";
        }

        if (state == RequestStatus.ASSIGNED) {
            return submitted
                    + "\n● Assistance assigned"
                    + "\n○ Assistance in progress"
                    + "\n○ Completion";
        }

        if (state == RequestStatus.IN_PROGRESS) {
            return submitted
                    + "\n● Assistance in progress"
                    + "\n○ Completion";
        }

        return submitted + "\nCurrent status: " + status(request);
    }

    private static String coordinatorName(
            AidRequest request
    ) {
        for (TaskAssignment assignment : request.getAssignments()) {
            if (assignment != null
                    && assignment.getCoordinator() != null) {
                return value(
                        assignment.getCoordinator().getFullName(),
                        ""
                );
            }
        }

        return "";
    }

    private static int integer(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException exception) {
            return -1;
        }
    }

    private User currentUser() {
        return ReliefApplication.getInstance().getCurrentUser();
    }

    private void message(String text) {
        messageLabel.setText(text);
        shown(messageLabel, true);
        pageScroll.setVvalue(0);
    }

    private static String value(
            String value,
            String fallback
    ) {
        return value == null || value.trim().isEmpty()
                ? fallback
                : value;
    }

    private static void shown(
            Node node,
            boolean visible
    ) {
        node.setVisible(visible);
        node.setManaged(visible);
    }

    private void navigate(String screen) {
        if (editing && dirty && !confirmDiscard()) {
            return;
        }

        try {
            App.setRoot(screen);
        } catch (IOException exception) {
            message(
                    "The " + screen + " screen is not available. "
                    + "Your request has not been changed."
            );
        }
    }

    @FXML
    private void openHome() {
        navigate("user_home");
    }

    @FXML
    private void openRequests() {
        navigate("my_requests");
    }

    @FXML
    private void openSafety() {
        navigate("safety");
    }

    @FXML
    private void openAccount() {
        navigate("account");
    }
}