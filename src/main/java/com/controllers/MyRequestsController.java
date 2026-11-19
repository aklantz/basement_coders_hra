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
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/** Shows the signed-in user's real requests in the empty or populated layout. */
public class MyRequestsController {
    private static final DateTimeFormatter DATE =
            DateTimeFormatter.ofPattern("MMM d, yyyy 'at' h:mm a", Locale.US);
    private static final String CARD = "-fx-background-color: white;"
            + "-fx-background-radius: 12px; -fx-border-color: #DCE5EB;"
            + "-fx-border-radius: 12px; -fx-padding: 18px;";
    private static final String SECONDARY = "-fx-background-color: white;"
            + "-fx-background-insets: 0; -fx-background-radius: 8px;"
            + "-fx-border-color: #DCE5EB; -fx-border-radius: 8px;"
            + "-fx-text-fill: #087F7A; -fx-font-size: 13px;"
            + "-fx-font-weight: bold; -fx-min-height: 48px; -fx-cursor: hand;";

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
    @FXML private Button closeOverlayButton;

    private Node previousFocus;

    @FXML
    private void initialize() {
        refreshRequests();
        Platform.runLater(() -> { if (!overlay.isVisible()) { requestsRoot.requestFocus(); } });
    }

    /** Re-read model data whenever a request is created or the page is refreshed. */
    @FXML
    private void refreshRequests() {
        User user = currentUser();
        if (user == null) {
            mainContent.setDisable(true);
            Platform.runLater(() -> navigate("login"));
            return;
        }
        List<AidRequest> requests = ReliefApplication.getInstance().getRequests();
        List<AidRequest> active = requestsForUser(requests, user, false);
        List<AidRequest> previous = requestsForUser(requests, user, true);
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
        createRequestButton.setText(empty
                ? "Create relief request" : "+  Create another request");
        supportHeading.setText(empty
                ? "Before you request support" : "Before you create another");
        supportMessage.setText(empty
                ? "Relief requests are for non-emergency needs. Assistance and timing are not guaranteed."
                : "Review your active requests before creating another for the same needs. Assistance and timing are not guaranteed.");
        AccountController.applyTextScale(requestsRoot);
    }

    private VBox requestCard(AidRequest request) {
        VBox card = new VBox(11);
        card.setStyle(CARD);
        Label badge = label(com.model.ResidentRequests.isCancelled(request) ? "CANCELLED" : statusText(request.getStatus()), 10, true);
        boolean completed = request.getStatus() == RequestStatus.FULFILLED;
        badge.setStyle(badge.getStyle() + "-fx-text-fill: "
                + (completed ? "#087F7A" : "#94600D") + ";-fx-background-color: "
                + (completed ? "#E4F3F0" : "#FFF3DC")
                + ";-fx-background-radius: 12px;-fx-padding: 5px 9px;");
        Label title = label(categoryText(request.getCategory()), 17, true);
        String id = request.getRequestId() == null ? "ID unavailable"
                : request.getRequestId().toString().substring(0, 8).toUpperCase(Locale.ROOT);
        Label reference = label(id + " · Submitted " + date(request), 11, false);
        Label household = label("Household & location", 12, true);
        Label location = label(request.getHouseholdSize() + " people · "
                + value(request.getStreetAddress(), "Location not recorded"), 13, false);
        Label update = label(com.model.ResidentRequests.isCancelled(request) ? "You cancelled this request." : statusDescription(request.getStatus()), 12, false);
        Button view = new Button("View request");
        view.setMaxWidth(Double.MAX_VALUE);
        view.setStyle(SECONDARY);
        view.setOnAction(event -> showRequest(request));
        card.getChildren().addAll(badge, title, reference, household, location, update, view);
        return card;
    }

    /** Opens a specific request only when it belongs to the current user. */
    public void openRequest(java.util.UUID requestId) {
        if (currentUser() == null) { navigate("login"); return; }
        List<AidRequest> requests = ReliefApplication.getInstance().getRequests();
        if (requests != null) {
            for (AidRequest request : requests) {
                if (belongsTo(request, currentUser()) && requestId != null
                        && requestId.equals(request.getRequestId())) {
                    showRequest(request);
                    return;
                }
            }
        }
        prepareOverlay("Request unavailable");
        detailFields.getChildren().setAll(label("This request could not be found in your account.", 14, false));
        closeOverlayButton.requestFocus();
    }

    private void showRequest(AidRequest request) {
        if (!belongsTo(request, currentUser())) { navigate("login"); return; }
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(App.class.getResource("request_detail.fxml"));
            javafx.scene.Parent view = loader.load();
            AccountController.applyTextScale(view);
            RequestDetailController controller = loader.getController();
            requestsRoot.getScene().setRoot(view);
            controller.openRequest(request.getRequestId());
        } catch (IOException exception) { showUnavailable(); }
    }

    @FXML
    private void openCreateRequest() {
        navigate(currentUser() == null ? "login" : "request_relief");
    }

    private void prepareOverlay(String title) {
        if (!overlay.isVisible()) {
            previousFocus = requestsRoot.getScene() == null
                    ? null : requestsRoot.getScene().getFocusOwner();
        }
        overlayTitle.setText(title);
        setShown(detailFields, true);
        closeOverlayButton.setText("Done");
        mainContent.setDisable(true);
        setShown(overlay, true);
        overlay.toFront();
        overlayScroll.setVvalue(0);
    }

    private void detail(String title, String text) {
        VBox group = new VBox(5, label(title, 12, true), label(text, 14, false));
        group.setPadding(new Insets(0, 0, 10, 0));
        detailFields.getChildren().add(group);
    }

    @FXML
    private void closeOverlay() {
        setShown(overlay, false);
        mainContent.setDisable(false);
        if (previousFocus != null && !previousFocus.isDisabled()) {
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

    @FXML private void openHome() { navigate("user_home"); }
    @FXML private void openSafety() { navigate("safety"); }
    @FXML private void openAccount() { navigate(currentUser() == null ? "login" : "account"); }

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
        prepareOverlay("Screen unavailable");
        detailFields.getChildren().setAll(label(
                "This screen is not available yet. You can continue reviewing your requests here.",
                14, false));
        closeOverlayButton.requestFocus();
    }

    private User currentUser() { return ReliefApplication.getInstance().getCurrentUser(); }

    private static void setShown(Node node, boolean show) {
        node.setVisible(show);
        node.setManaged(show);
    }

    private static Label label(String text, int size, boolean bold) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setMinHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setStyle("-fx-font-size: " + size + "px;-fx-text-fill: "
                + (bold ? "#142D45" : "#536879") + ";"
                + (bold ? "-fx-font-weight: bold;" : ""));
        return label;
    }

    private static String date(AidRequest request) {
        return request.getCreatedAt() == null ? "date unavailable" : DATE.format(request.getCreatedAt());
    }

    private static String value(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private static String statusDescription(RequestStatus status) {
        if (status == null) { return "No status has been recorded yet."; }
        switch (status) {
            case SUBMITTED: return "Your request is waiting for coordinator review. No assistance has been assigned yet.";
            case ASSIGNED: return "Your request has been assigned. Review its details for the latest recorded status.";
            case IN_PROGRESS: return "Assistance for this request is in progress.";
            case FULFILLED: return "This request has been marked fulfilled.";
            case CLOSED: return "This request is closed.";
            case EXPIRED: return "This request has expired. Review your needs before creating another request.";
            default: return "No status has been recorded yet.";
        }
    }
    /** Selects this user's active or previous requests, newest first. */
    private static List<AidRequest> requestsForUser(
            List<AidRequest> requests, User user, boolean previous) {
        List<AidRequest> matches = new ArrayList<>();
        if (requests != null) {
            for (AidRequest request : requests) {
                if (belongsTo(request, user)
                        && isPrevious(request.getStatus()) == previous) {
                    matches.add(request);
                }
            }
        }
        matches.sort(Comparator.comparing(
                AidRequest::getCreatedAt,
                Comparator.nullsLast(Comparator.<LocalDateTime>reverseOrder())));
        return matches;
    }

    /** User IDs, rather than Java object identity or names, determine ownership. */
    private static boolean belongsTo(AidRequest request, User user) {
        return user != null && user.getUserId() != null
                && request != null && request.getRequester() != null
                && user.getUserId().equals(request.getRequester().getUserId());
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
            case SUBMITTED: return "AWAITING REVIEW";
            case ASSIGNED: return "ASSIGNED";
            case IN_PROGRESS: return "IN PROGRESS";
            case FULFILLED: return "COMPLETED";
            case CLOSED: return "CLOSED";
            case EXPIRED: return "EXPIRED";
            default: return "STATUS UNAVAILABLE";
        }
    }

    private static String categoryText(RequestCategory category) {
        if (category == null) {
            return "Relief request";
        }
        switch (category) {
            case FOOD: return "Food";
            case WATER: return "Drinking water";
            case MEDICAL: return "Medical supplies";
            case RESCUE: return "Rescue support";
            case TRANSPORT: return "Transport";
            case SUPPLIES: return "Essential supplies";
            case POWER: return "Power and charging";
            case DEBRIS_REMOVAL: return "Debris removal";
            case OTHER: return "Other essentials";
            default: return category.name().toLowerCase(Locale.ROOT).replace('_', ' ');
        }
    }

}
