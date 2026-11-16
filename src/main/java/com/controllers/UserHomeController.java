package com.controllers;

import java.io.IOException;
import com.hurricane.App;
import com.model.ReliefApplication;
import com.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class UserHomeController {

    @FXML
    private Label lbl_welcome;

    @FXML
    private void initialize() {
        User user = ReliefApplication.getInstance().getCurrentUser();
        String firstName = user.getFullName().trim().split(" ")[0];
        lbl_welcome.setText("Good morning, " + firstName);
    }

    @FXML
    private void btnLogoutClicked() throws IOException {
        ReliefApplication.getInstance().logout();
        App.setRoot("splashscreen");
    }
}