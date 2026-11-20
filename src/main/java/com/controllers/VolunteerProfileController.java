package com.controllers;

import java.io.IOException;
import com.hurricane.App;
import com.model.ReliefApplication;
import javafx.fxml.FXML;

public class VolunteerProfileController {

    @FXML
    private VolunteerNavController volunteerNavController;

    @FXML
    private void initialize() {
        volunteerNavController.setActive("account");
    }

    @FXML
    private void btnSignOutClicked() throws IOException {
        ReliefApplication.getInstance().logout();
        App.setRoot("splashscreen");
    }
}