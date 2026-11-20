package com.controllers;

import java.io.IOException;
import com.hurricane.App;
import javafx.fxml.FXML;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class VolunteerPreviewController {

    @FXML
    private VolunteerNavController volunteerNavController;

    @FXML
    private StackPane acceptOverlay;

    @FXML
    private StackPane declineOverlay;

    @FXML
    private VBox reasonList;

    private ToggleGroup reasonGroup = new ToggleGroup();

    @FXML
    private void initialize() {
        volunteerNavController.setActive("tasks");

        String[] reasons = {"Too far away", "Not available now",
                "Missing skills or supplies", "Safety concern", "Other"};

        for (String reason : reasons) {
            ToggleButton button = new ToggleButton(reason);
            button.setToggleGroup(reasonGroup);
            button.getStyleClass().add("reason-button");
            button.setMaxWidth(Double.MAX_VALUE);
            reasonList.getChildren().add(button);
        }
    }

    @FXML
    private void btnAcceptClicked() {
        acceptOverlay.setVisible(true);
    }

    @FXML
    private void btnNotNowClicked() {
        acceptOverlay.setVisible(false);
    }

    @FXML
    private void btnConfirmClicked() throws IOException {
        App.setRoot("volunteer_task_accepted");
    }

    @FXML
    private void btnNotForMeClicked() {
        declineOverlay.setVisible(true);
    }

    @FXML
    private void btnKeepBrowsingClicked() {
        declineOverlay.setVisible(false);
    }

    @FXML
    private void btnSubmitReasonClicked() throws IOException {
        // The reason is not saved yet.
        App.setRoot("volunteer_recommended");
    }

    @FXML
    private void back() throws IOException {
        App.setRoot("volunteer_recommended");
    }
}