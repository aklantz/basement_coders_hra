package com.controllers;

import com.hurricane.App;
import com.model.AidRequest;
import com.model.ReliefApplication;
import com.model.RequestCategory;
import com.model.RequestStatus;
import com.model.User;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

/**
 * Displays and manages the signed-in user's relief requests.
 */
public class MyRequestsController {

    private static final DateTimeFormatter DATE =
            DateTimeFormatter.ofPattern(
                    "MMM d, yyyy 'at' h:mm a",
                    Locale.US
            );

    private static final String CARD =
            "-fx-background-color: white;"
            + "-fx-background-radius: 12px;"
            + "-fx-border-color: #DCE5EB;"
            + "-fx-border-radius: 12px;"
            + "-fx-padding: 18px;";

    private static final String SECONDARY =
            "-fx-background-color: white;"
            + "-fx-background-insets: 0;"
            + "-fx-background-radius: 8px;"
            + "-fx-border-color: #DCE5EB;"
            + "-fx-border-radius: 8px;"
            + "-fx-text-fill: #087F7A;"
            + "-fx-font-size: 13px;"
            + "-fx-font-weight: bold;"
            + "-fx-min-height: 48px;"
            + "-fx-cursor: hand;";

    @FXML private StackPane requestsRoot;
    @FXML private BorderPane mainContent;

    @FXML private Label activeCountLabel;
    @FXML private Label previousCountLabel;

    @FXML private VBox emptyState;
    @FXML private VBox populatedState;
    @FXML private VBox activeRequests;
    @FXML private VBox previousRequests;

    @FXML private Label noActiveLabel;
    @FXML private Label previousHeading;

    @FXML private Button createRequestButton;
    @FXML private Label supportHeading;
    @FXML private Label supportMessage;

    @FXML private StackPane overlay;
    @FXML private ScrollPane overlayScroll;
    @FXML private Label overlayTitle;

    @FXML private VBox detailFields;
    @FXML private VBox createForm;

    @FXML private ComboBox<RequestCategory> categoryField;
    @FXML private ComboBox<Integer> householdSizeField;
    @FXML private TextField addressField;
    @FXML private TextArea noteField;
    @FXML private Label formError;

    @FXML private Button submitRequestButton;
    @FXML private Button closeOverlayButton;

    private Node previousFocus;
    private UUID formOwnerId;

    @FXML
    private void initialize() {
        categoryField.getItems().setAll(
                RequestCategory.FOOD,
                RequestCategory.WATER,
                RequestCategory.MEDICAL,
                RequestCategory.TRANSPORT,
                RequestCategory.SUPPLIES,
                RequestCategory.POWER,
                RequestCategory.DEBRIS_REMOVAL,
                RequestCategory.OTHER
        );

        categoryField.setConverter(
                new StringConverter<RequestCategory>() {
                    @Override
                    public String toString(RequestCategory value) {
                        return value == null
                                ? ""
                                : categoryText(value);
                    }

                    @Override
                    public RequestCategory fromString(String value) {
                        throw new UnsupportedOperationException(
                                "Choose a listed category."
                        );
                    }
                }
        );

        for (int count = 1; count <= 20; count++) {
            householdSizeField.getItems().add(count);
        }

        householdSizeField.setEditable(true);

        refreshRequests();

        Platform.runLater(() -> requestsRoot.requestFocus());
    }

    /**
     * Reloads the user's requests and updates both screen states.
     */
    @FXML
    private void refreshRequests() {
        User user = currentUser();

        if (user == null) {
            mainContent.setDisable(true);
            Platform.runLater(() -> navigate("login"));
            return;
        }

        List<AidRequest> requests =
                ReliefApplication.getInstance().getRequests();

        List<AidRequest> active =
                requestsForUser(requests, user, false);

        List<AidRequest> previous =
                requestsForUser(requests, user, true);

        boolean empty = active.isEmpty() && previous.isEmpty();

        activeCountLabel.setText("Active · " + active.size());
        previousCountLabel.setText("Previous · " + previous.size());

        setShown(emptyState, empty);
        setShown(populatedState, !empty);

        activeRequests.getChildren().clear();
        previousRequests.getChildren().clear();

        for (AidRequest request : active) {
            activeRequests.getChildren().add(requestCard(request));
        }

        for (AidRequest request : previous) {
            previousRequests.getChildren().add(requestCard(request));
        }

        setShown(noActiveLabel, active.isEmpty() && !empty);
        setShown(previousHeading, !previous.isEmpty());
        setShown(previousRequests, !previous.isEmpty());

        createRequestButton.setText(
                empty
                        ? "Create relief request"
                        : "+  Create another request"
        );

        supportHeading.setText(
                empty
                        ? "Before you request support"
                        : "Before you create another"
        );

        supportMessage.setText(
                empty
                        ? "Relief requests are for non-emergency needs. "
                          + "Assistance and timing are not guaranteed."
                        : "Review your active requests before creating "
                          + "another for the same needs. Assistance and "
                          + "timing are not guaranteed."
        );
    }

    /**
     * Creates a card using actual request information.
     */
    private VBox requestCard(AidRequest request) {
        VBox card = new VBox(11);
        card.setStyle(CARD);

        Label badge = label(
                statusText(request.getStatus()),
                10,
                true
        );

        boolean completed =
                request.getStatus() == RequestStatus.FULFILLED;

        badge.setStyle(
                badge.getStyle()
                + "-fx-text-fill: "
                + (completed ? "#087F7A" : "#94600D")
                + ";-fx-background-color: "
                + (completed ? "#E4F3F0" : "#FFF3DC")
                + ";-fx-background-radius: 12px;"
                + "-fx-padding: 5px 9px;"
        );

        Label title = label(
                categoryText(request.getCategory()),
                17,
                true
        );

        String id = request.getRequestId() == null
                ? "ID unavailable"
                : request.getRequestId()
                         .toString()
                         .substring(0, 8)
                         .toUpperCase(Locale.ROOT);

        Label reference = label(
                id + " · Submitted " + date(request),
                11,
                false
        );

        Label household = label(
                "Household & location",
                12,
                true
        );

        Label location = label(
                request.getHouseholdSize()
                        + " people · "
                        + value(
                                request.getStreetAddress(),
                                "Location not recorded"
                        ),
                13,
                false
        );

        Label update = label(
                statusDescription(request.getStatus()),
                12,
                false
        );

        Button view = new Button("View request");
        view.setMaxWidth(Double.MAX_VALUE);
        view.setStyle(SECONDARY);
        view.setOnAction(event -> showRequest(request));

        card.getChildren().addAll(
                badge,
                title,
                reference,
                household,
                location,
                update,
                view
        );

        return card;
    }

    private void showRequest(AidRequest request) {
        if (!belongsTo(request, currentUser())) {
            navigate("login");
            return;
        }

        prepareOverlay("Your relief request", false);
        detailFields.getChildren().clear();

        detail(
                "Request ID",
                request.getRequestId() == null
                        ? "Not recorded"
                        : request.getRequestId().toString()
        );

        detail("Status", statusText(request.getStatus()));

        detail(
                "Requested support",
                categoryText(request.getCategory())
        );

        detail("Submitted", date(request));

        detail(
                "People needing support",
                String.valueOf(request.getHouseholdSize())
        );

        detail(
                "Location",
                value(request.getStreetAddress(), "Not recorded")
        );

        detail(
                "Your note",
                value(request.getNote(), "No note added")
        );

        detail(
                "Latest status",
                statusDescription(request.getStatus())
        );

        closeOverlayButton.requestFocus();
    }

    @FXML
    private void openCreateRequest() {
        User user = currentUser();

        if (user == null) {
            navigate("login");
            return;
        }

        formOwnerId = user.getUserId();

        categoryField.setValue(null);

        householdSizeField.setValue(1);
        householdSizeField.getEditor().setText("1");

        addressField.setText(value(user.getHomeAddress(), ""));
        noteField.clear();

        setShown(formError, false);

        prepareOverlay("Create relief request", true);
        categoryField.requestFocus();
    }

    @FXML
    private void submitRequest() {
        User user = currentUser();

        if (user == null
                || formOwnerId == null
                || !formOwnerId.equals(user.getUserId())) {
            navigate("login");
            return;
        }

        int size;

        try {
            size = Integer.parseInt(
                    householdSizeField.getEditor()
                                      .getText()
                                      .trim()
            );

            if (size < 1) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException exception) {
            formError(
                    "Choose a positive whole number of people."
            );
            return;
        }

        if (categoryField.getValue() == null) {
            formError("Choose the kind of support you need.");
            return;
        }

        if (addressField.getText().trim().isEmpty()) {
            formError(
                    "Enter a location where support can reach you."
            );
            return;
        }

        submitRequestButton.setDisable(true);

        try {
            AidRequest created =
                    ReliefApplication.getInstance().createAidRequest(
                            categoryField.getValue(),
                            size,
                            addressField.getText().trim(),
                            noteField.getText().trim()
                    );

            if (created == null) {
                formError(
                        "Your request could not be saved. "
                        + "Your entries are still here; please try again."
                );
                return;
            }

            closeOverlay();
            refreshRequests();
            showRequest(created);

        } catch (RuntimeException exception) {
            formError(
                    "Your request could not be saved. "
                    + "Your entries are still here; please try again."
            );
        } finally {
            submitRequestButton.setDisable(false);
        }
    }

    private void prepareOverlay(String title, boolean creating) {
        if (!overlay.isVisible()) {
            previousFocus = requestsRoot.getScene() == null
                    ? null
                    : requestsRoot.getScene().getFocusOwner();
        }

        overlayTitle.setText(title);

        setShown(createForm, creating);
        setShown(detailFields, !creating);
        setShown(submitRequestButton, creating);

        closeOverlayButton.setText(
                creating ? "Cancel" : "Done"
        );

        mainContent.setDisable(true);

        setShown(overlay, true);
        overlay.toFront();

        overlayScroll.setVvalue(0);
    }

    private void detail(String title, String text) {
        VBox group = new VBox(
                5,
                label(title, 12, true),
                label(text, 14, false)
        );

        group.setPadding(new Insets(0, 0, 10, 0));
        detailFields.getChildren().add(group);
    }

    private void formError(String text) {
        formError.setText(text);
        setShown(formError, true);
        formError.requestFocus();
    }

    @FXML
    private void closeOverlay() {
        setShown(overlay, false);
        mainContent.setDisable(false);

        formOwnerId = null;

        if (previousFocus != null
                && !previousFocus.isDisabled()) {
            previousFocus.requestFocus();
        } else {
            requestsRoot.requestFocus();
        }
    }

    @FXML
    private void handleOverlayKey(KeyEvent event) {
        if (event.getCode() == KeyCode.ESCAPE) {
            closeOverlay();
            event.consume();
        }
    }

    @FXML
    private void openHome() {
        navigate("user_home");
    }

    @FXML
    private void openSafety() {
        navigate("safety");
    }

    @FXML
    private void openAccount() {
        navigate(currentUser() == null ? "login" : "account");
    }

    private void navigate(String filename) {
        if (App.class.getResource(filename + ".fxml") == null) {
            showUnavailable();
            return;
        }

        try {
            App.setRoot(filename);
        } catch (IOException exception) {
            showUnavailable();
        }
    }

    private void showUnavailable() {
        prepareOverlay("Screen unavailable", false);

        detailFields.getChildren().setAll(
                label(
                        "This screen is not available yet. "
                        + "You can continue reviewing your requests here.",
                        14,
                        false
                )
        );

        closeOverlayButton.requestFocus();
    }

    private User currentUser() {
        return ReliefApplication.getInstance().getCurrentUser();
    }

    private static void setShown(Node node, boolean show) {
        node.setVisible(show);
        node.setManaged(show);
    }

    private static Label label(
            String text,
            int size,
            boolean bold
    ) {
        Label label = new Label(text);

        label.setWrapText(true);

        label.setMinHeight(
                javafx.scene.layout.Region.USE_PREF_SIZE
        );

        label.setMaxWidth(Double.MAX_VALUE);

        label.setStyle(
                "-fx-font-size: " + size + "px;"
                + "-fx-text-fill: "
                + (bold ? "#142D45" : "#536879")
                + ";"
                + (bold ? "-fx-font-weight: bold;" : "")
        );

        return label;
    }

    private static String date(AidRequest request) {
        return request.getCreatedAt() == null
                ? "date unavailable"
                : DATE.format(request.getCreatedAt());
    }

    private static String value(
            String value,
            String fallback
    ) {
        return value == null || value.trim().isEmpty()
                ? fallback
                : value.trim();
    }

    private static String statusDescription(
            RequestStatus status
    ) {
        if (status == null) {
            return "No status has been recorded yet.";
        }

        switch (status) {
            case SUBMITTED:
                return "Your request is waiting for coordinator review. "
                        + "No assistance has been assigned yet.";

            case ASSIGNED:
                return "Your request has been assigned. "
                        + "Review its details for the latest recorded status.";

            case IN_PROGRESS:
                return "Assistance for this request is in progress.";

            case FULFILLED:
                return "This request has been marked fulfilled.";

            case CLOSED:
                return "This request is closed.";

            case EXPIRED:
                return "This request has expired. "
                        + "Review your needs before creating another request.";

            default:
                return "No status has been recorded yet.";
        }
    }

    /**
     * Returns the user's active or previous requests, newest first.
     */
    private static List<AidRequest> requestsForUser(
            List<AidRequest> requests,
            User user,
            boolean previous
    ) {
        List<AidRequest> matches = new ArrayList<>();

        if (requests != null) {
            for (AidRequest request : requests) {
                if (belongsTo(request, user)
                        && isPrevious(request.getStatus()) == previous) {
                    matches.add(request);
                }
            }
        }

        matches.sort(
                Comparator.comparing(
                        AidRequest::getCreatedAt,
                        Comparator.nullsLast(
                                Comparator.<LocalDateTime>reverseOrder()
                        )
                )
        );

        return matches;
    }

    /**
     * Checks ownership using the user's unique ID.
     */
    private static boolean belongsTo(
            AidRequest request,
            User user
    ) {
        return user != null
                && user.getUserId() != null
                && request != null
                && request.getRequester() != null
                && user.getUserId().equals(
                        request.getRequester().getUserId()
                );
    }

    private static boolean isPrevious(RequestStatus status) {
        return status == RequestStatus.FULFILLED
                || status == RequestStatus.CLOSED
                || status == RequestStatus.EXPIRED;
    }

    private static String statusText(RequestStatus status) {
        if (status == null) {
            return "STATUS UNAVAILABLE";
        }

        switch (status) {
            case SUBMITTED:
                return "AWAITING REVIEW";

            case ASSIGNED:
                return "ASSIGNED";

            case IN_PROGRESS:
                return "IN PROGRESS";

            case FULFILLED:
                return "COMPLETED";

            case CLOSED:
                return "CLOSED";

            case EXPIRED:
                return "EXPIRED";

            default:
                return "STATUS UNAVAILABLE";
        }
    }

    private static String categoryText(
            RequestCategory category
    ) {
        if (category == null) {
            return "Relief request";
        }

        switch (category) {
            case FOOD:
                return "Food";

            case WATER:
                return "Drinking water";

            case MEDICAL:
                return "Medical supplies";

            case RESCUE:
                return "Rescue support";

            case TRANSPORT:
                return "Transport";

            case SUPPLIES:
                return "Essential supplies";

            case POWER:
                return "Power and charging";

            case DEBRIS_REMOVAL:
                return "Debris removal";

            case OTHER:
                return "Other essentials";

            default:
                return category.name()
                               .toLowerCase(Locale.ROOT)
                               .replace('_', ' ');
        }
    }
}