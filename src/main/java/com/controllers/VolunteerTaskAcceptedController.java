package com.controllers;

import java.io.IOException;
import com.hurricane.App;
import javafx.fxml.FXML;

public class VolunteerTaskAcceptedController {

    @FXML
    private VolunteerNavController volunteerNavController;

    @FXML
    private void initialize() {
        volunteerNavController.setActive("tasks");
    }

    @FXML
private void btnDirectionsClicked() throws IOException {
    App.setRoot("volunteer_directions");
}

@FXML
private void btnStartClicked() throws IOException {
    App.setRoot("volunteer_directions");
}

    @FXML
    private void back() throws IOException {
        App.setRoot("volunteer_home");
    }
}