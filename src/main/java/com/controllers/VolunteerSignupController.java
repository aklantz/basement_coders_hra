package com.controllers;

import java.io.IOException;
import com.hurricane.App;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class VolunteerSignupController {

    @FXML
    private TextField fullNameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField phoneField;

    @FXML
    private TextField zipField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField visiblePasswordField;

    @FXML
    private VBox errorBox;

    @FXML
    private Label lbl_error;

    @FXML
    private void initialize() {
        visiblePasswordField.textProperty()
                .bindBidirectional(passwordField.textProperty());
    }

    @FXML
    private void togglePasswordVisibility() {
        boolean showPassword = !visiblePasswordField.isVisible();

        visiblePasswordField.setVisible(showPassword);
        visiblePasswordField.setManaged(showPassword);

        passwordField.setVisible(!showPassword);
        passwordField.setManaged(!showPassword);
    }

    @FXML
    private void btnContinueClicked() throws IOException {
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String zipCodes = zipField.getText().trim();
        String password = passwordField.getText();

        if (fullName.isEmpty() || email.isEmpty() || phone.isEmpty()
                || zipCodes.isEmpty() || password.isEmpty()) {
            showError("Please fill in every field.");
            return;
        }

        if (password.length() < 12) {
            showError("Password must be at least 12 characters.");
            return;
        }

        VolunteerSignupData.fullName = fullName;
        VolunteerSignupData.email = email;
        VolunteerSignupData.phone = phone;
        VolunteerSignupData.zipCodes = zipCodes;
        VolunteerSignupData.password = password;

        App.setRoot("volunteer_skills");
    }

    @FXML
    private void back() throws IOException {
        App.setRoot("splashscreen");
    }

    private void showError(String message) {
        lbl_error.setText(message);
        errorBox.setManaged(true);
        errorBox.setVisible(true);
    }
}