package com.controllers;

import java.io.IOException;
import com.hurricane.App;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class VolunteerPendingController {

    @FXML
    private Label emailLabel;

    @FXML
    private void initialize() {
        if (VolunteerSignupData.email != null) {
            emailLabel.setText("We’ll send an update to " + VolunteerSignupData.email
                    + " when your account is reviewed.");
        }
    }

    @FXML
    private void btnReviewClicked() throws IOException {
        App.setRoot("volunteer_signup");
    }

    @FXML
    private void btnStormUpdatesClicked() throws IOException {
        App.setRoot("splashscreen");
    }
}