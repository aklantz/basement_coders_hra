package com.controllers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import com.hurricane.App;
import com.model.ReliefApplication;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

/**
 * Controls safety guidance, offline checklist saving,
 * shelter contact information and navigation.
 */
public class SafetyController {

    // Change these if your destination files have different names.
    private static final String REQUESTS_SCREEN = "my_requests";
    private static final String ACCOUNT_SCREEN = "account";

    private static final String CHECKLIST =
            "HURRICANE SAFETY CHECKLIST\n\n"
            + "1. Follow evacuation instructions promptly. "
            + "Bring essential medicines and plan a route you can use.\n\n"
            + "2. Avoid walking or driving through flooded areas.\n\n"
            + "3. Run generators outdoors, at least 20 feet "
            + "from doors, windows and vents.\n\n"
            + "4. Follow official updates before returning "
            + "after an evacuation.\n\n"
            + "For immediate emergency assistance in the US, "
            + "call 911 from a phone. This app does not dispatch rescue.\n\n"
            + "This file does not contain live storm alerts, "
            + "road conditions or shelter availability.\n\n"
            + "Sources:\n"
            + "https://www.ready.gov/hurricanes\n"
            + "https://www.noaa.gov/use-caution-after-storms\n"
            + "https://www.911.gov/calling-911/\n";

    private final Path checklistFile = Path.of(
            System.getProperty("user.home"),
            "HurricaneTracker",
            "safety-checklist.txt"
    );

    private Node previousFocus;

    @FXML
    private StackPane safetyRoot;

    @FXML
    private BorderPane mainContent;

    @FXML
    private Label offlineTitleLabel;

    @FXML
    private Label offlineDescriptionLabel;

    @FXML
    private StackPane informationOverlay;

    @FXML
    private Label informationTitleLabel;

    @FXML
    private Label informationMessageLabel;

    @FXML
    private Button informationDoneButton;

    @FXML
    private void initialize() {
        if (Files.isRegularFile(checklistFile)) {
            showSavedChecklistStatus();
        }
    }

    @FXML
    private void showEmergencyInstructions() {
        showInformation(
                "Emergency assistance",
                "For immediate police, fire or medical help "
                + "in the US, call 911 from a phone.\n\n"
                + "This desktop app does not place the call "
                + "or dispatch emergency services."
        );
    }

    @FXML
    private void saveChecklist() {
        try {
            Files.createDirectories(checklistFile.getParent());

            Files.writeString(
                    checklistFile,
                    CHECKLIST,
                    StandardCharsets.UTF_8
            );

            showSavedChecklistStatus();

            showInformation(
                    "Checklist saved",
                    "Your safety checklist is available offline at:\n\n"
                    + checklistFile
            );

        } catch (IOException | SecurityException exception) {
            showInformation(
                    "Unable to save checklist",
                    "The checklist could not be saved to this device. "
                    + "Check that your application has permission "
                    + "to write to your user folder."
            );
        }
    }

    private void showSavedChecklistStatus() {
        offlineTitleLabel.setText("Offline safety checklist saved");

        offlineDescriptionLabel.setText(
                "Checklist saved to this device. Maps, shelter "
                + "availability and storm alerts still require "
                + "current information."
        );
    }

    @FXML
    private void showShelterContact() {
        /*
         * This entry is sample data from the prototype.
         * Replace it with your verified shelter model/service.
         */
        showInformation(
                "Shelter contact",
                "The Riverside shelter entry currently contains "
                + "sample prototype details.\n\n"
                + "Confirm real shelter locations, phone numbers "
                + "and availability with local emergency management "
                + "before traveling."
        );
    }

    @FXML
    private void openHome() throws IOException {
        App.setRoot("user_home");
    }

    @FXML
    private void openRequests() throws IOException {
        if (isGuest()) {
            App.setRoot("login");
            return;
        }

        openScreen(REQUESTS_SCREEN);
    }

    @FXML
    private void openAccount() throws IOException {
        if (isGuest()) {
            App.setRoot("login");
            return;
        }

        openScreen(ACCOUNT_SCREEN);
    }

    private boolean isGuest() {
        return ReliefApplication.getInstance()
                .getCurrentUser() == null;
    }

    private void openScreen(String filename) throws IOException {
        if (App.class.getResource(filename + ".fxml") == null) {
            showInformation(
                    "Screen unavailable",
                    "Cannot find " + filename + ".fxml. "
                    + "Check the destination filename "
                    + "in SafetyController."
            );
            return;
        }

        App.setRoot(filename);
    }

    private void showInformation(String title, String message) {
        if (safetyRoot.getScene() != null) {
            previousFocus = safetyRoot.getScene().getFocusOwner();
        }

        informationTitleLabel.setText(title);
        informationMessageLabel.setText(message);

        mainContent.setDisable(true);

        informationOverlay.setManaged(true);
        informationOverlay.setVisible(true);
        informationOverlay.toFront();

        informationDoneButton.requestFocus();
    }

    @FXML
    private void closeInformation() {
        informationOverlay.setVisible(false);
        informationOverlay.setManaged(false);

        mainContent.setDisable(false);

        if (previousFocus != null && !previousFocus.isDisabled()) {
            previousFocus.requestFocus();
        } else {
            safetyRoot.requestFocus();
        }
    }

    @FXML
    private void handleOverlayKey(KeyEvent event) {
        if (event.getCode() == KeyCode.ESCAPE) {
            closeInformation();
            event.consume();
        }
    }
}