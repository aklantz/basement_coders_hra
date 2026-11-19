package com.controllers;

import java.io.IOException;
import com.hurricane.App;
import com.model.ReliefApplication;
import com.model.User;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

/**
 * Controls the resident home screen, navigation,
 * and the container for the functional map.
 */
public class UserHomeController {

    /*
     * EXAMPLE FILENAMES:
     * Replace these with your actual FXML filenames.
     * Do not include ".fxml".
     */
    private static final String REQUEST_RELIEF_SCREEN = "request_relief";
    private static final String REQUESTS_SCREEN = "my_requests";
    private static final String SHELTERS_SCREEN = "shelters";
    private static final String SAFETY_SCREEN = "safety";
    private static final String ACCOUNT_SCREEN = "account";

    @FXML
    private Label lbl_welcome;

    @FXML
    private StackPane mapContainer;

    @FXML
    private Button findShelterButton;

    @FXML
    private Button safetyChecklistButton;

    @FXML
    private HBox shelterRow;

    @FXML
    private HBox safetyRow;

    @FXML
    private void initialize() {
        updateWelcomeMessage();

        // Keep each row's arrow aligned to the button's right edge.
        shelterRow.prefWidthProperty().bind(
                findShelterButton.widthProperty().subtract(2)
        );

        safetyRow.prefWidthProperty().bind(
                safetyChecklistButton.widthProperty().subtract(2)
        );

        /*
         * Initialize your map here once its source is available,
         * then pass its JavaFX view to installMap(...).
         */
    }

    private void updateWelcomeMessage() {
        User user = ReliefApplication.getInstance().getCurrentUser();

        if (user == null
                || user.getFullName() == null
                || user.getFullName().isBlank()) {
            lbl_welcome.setText("Welcome");
            return;
        }

        String firstName = user.getFullName()
                .trim()
                .split("\\s+")[0];

        lbl_welcome.setText("Good morning, " + firstName);
    }

    @FXML
    private void openRequestRelief() throws IOException {
        openScreen(REQUEST_RELIEF_SCREEN);
    }

    @FXML
    private void openRequests() throws IOException {
        openScreen(REQUESTS_SCREEN);
    }

    @FXML
    private void openShelters() throws IOException {
        openScreen(SHELTERS_SCREEN);
    }

    @FXML
    private void openSafety() throws IOException {
        openScreen(SAFETY_SCREEN);
    }

    @FXML
    private void openAccount() throws IOException {
        openScreen(ACCOUNT_SCREEN);
    }

    /**
     * Opens a screen if its FXML resource exists.
     */
    private void openScreen(String filename) throws IOException {
        if (App.class.getResource(filename + ".fxml") == null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);

            alert.setTitle("Screen unavailable");
            alert.setHeaderText("This screen could not be opened");
            alert.setContentText(
                    "Cannot find " + filename + ".fxml.\n\n"
                    + "Check the destination filename in "
                    + "UserHomeController and make sure the file "
                    + "is in src/main/resources/com/hurricane/."
            );

            if (mapContainer.getScene() != null
                    && mapContainer.getScene().getWindow() != null) {
                alert.initOwner(
                        mapContainer.getScene().getWindow()
                );
            }

            alert.showAndWait();
            return;
        }

        App.setRoot(filename);
    }

    /**
     * Embeds an existing JavaFX map view.
     * Call this on the JavaFX Application Thread.
     *
     * @param mapView the map control or map FXML root
     */
    public void installMap(Node mapView) {
        if (mapView == null) {
            throw new IllegalArgumentException(
                    "The map view cannot be null."
            );
        }

        if (mapView.getParent() != null
                && mapView.getParent() != mapContainer) {
            throw new IllegalArgumentException(
                    "The map view is already attached "
                    + "to another layout."
            );
        }

        if (mapView instanceof Region) {
            Region region = (Region) mapView;

            region.setMinSize(0, 0);
            region.setMaxSize(
                    Double.MAX_VALUE,
                    Double.MAX_VALUE
            );
        }

        mapContainer.getChildren().setAll(mapView);
    }

    @FXML
    private void btnLogoutClicked() throws IOException {
        ReliefApplication.getInstance().logout();
        App.setRoot("splashscreen");
    }
}