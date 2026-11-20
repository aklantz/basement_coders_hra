package com.controllers;

import java.io.IOException;
import com.hurricane.App;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class VolunteerNavController {

    @FXML
    private Button homeButton;

    @FXML
    private Button tasksButton;

    @FXML
    private Button sheltersButton;

    @FXML
    private Button accountButton;

    public void setActive(String tab) {
        mark(homeButton, tab.equals("home"));
        mark(tasksButton, tab.equals("tasks"));
        mark(sheltersButton, tab.equals("shelters"));
        mark(accountButton, tab.equals("account"));
    }

    private void mark(Button button, boolean active) {
        button.getStyleClass().remove("nav-active");
        if (active) {
            button.getStyleClass().add("nav-active");
        }
    }

    @FXML
    private void goHome() throws IOException {
        App.setRoot("volunteer_home");
    }

    @FXML
    private void goTasks() throws IOException {
        App.setRoot("volunteer_tasks");
}

    @FXML
    private void goShelters() {
        // No volunteer shelters screen yet
    }

    @FXML
    private void goAccount() throws IOException {
        App.setRoot("volunteer_profile");
}
}