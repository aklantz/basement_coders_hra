package com.controllers;

import java.io.IOException;
import com.hurricane.App;
import javafx.fxml.FXML;

public class VolunteerOfferController {

    @FXML
    private VolunteerNavController volunteerNavController;

    @FXML
    private void initialize() {
        volunteerNavController.setActive("tasks");
    }

    @FXML
    private void btnAcceptClicked() throws IOException {
        App.setRoot("volunteer_task_accepted");
    }

    @FXML
    private void btnDeclineClicked() throws IOException {
        App.setRoot("volunteer_recommended");
    }

    @FXML
    private void back() throws IOException {
        App.setRoot("volunteer_home");
    }
}