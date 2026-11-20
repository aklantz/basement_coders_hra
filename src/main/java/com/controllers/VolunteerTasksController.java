package com.controllers;

import java.io.IOException;
import com.hurricane.App;
import javafx.fxml.FXML;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;

public class VolunteerTasksController {

    @FXML
    private VolunteerNavController volunteerNavController;

    @FXML
    private ToggleButton activeButton;

    @FXML
    private ToggleButton completedButton;

    @FXML
    private VBox activeList;

    @FXML
    private VBox completedList;

    @FXML
    private VBox emptyList;

    // Sample data. Set this to false to preview the empty state.
    private boolean hasActiveTask = true;

    @FXML
    private void initialize() {
        volunteerNavController.setActive("tasks");
        activeButton.setText(hasActiveTask ? "Active (1)" : "Active");
        showActive();
    }

    @FXML
    private void showActive() {
        activeButton.setSelected(true);
        completedButton.setSelected(false);
        show(activeList, hasActiveTask);
        show(emptyList, !hasActiveTask);
        show(completedList, false);
    }

    @FXML
    private void showCompleted() {
        activeButton.setSelected(false);
        completedButton.setSelected(true);
        show(activeList, false);
        show(emptyList, false);
        show(completedList, true);
    }

    private void show(VBox list, boolean visible) {
        list.setVisible(visible);
        list.setManaged(visible);
    }

    @FXML
    private void btnViewTaskClicked() throws IOException {
        App.setRoot("volunteer_task_accepted");
    }

    @FXML
    private void btnBrowseClicked() throws IOException {
        App.setRoot("volunteer_recommended");
    }
}