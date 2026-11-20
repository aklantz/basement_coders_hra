package com.controllers;

import java.io.IOException;
import com.hurricane.App;
import javafx.fxml.FXML;

public class VolunteerDirectionsController {

    @FXML
    private void btnArrivedClicked() throws IOException {
        App.setRoot("volunteer_complete");
    }

    @FXML
    private void btnDetailsClicked() throws IOException {
        App.setRoot("volunteer_task_accepted");
    }

    @FXML
    private void back() throws IOException {
        App.setRoot("volunteer_task_accepted");
    }
}