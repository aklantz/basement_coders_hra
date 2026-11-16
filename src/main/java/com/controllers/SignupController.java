package com.controllers;

import java.io.IOException;
import java.time.LocalDate;
import com.hurricane.App;
import com.model.ReliefApplication;
import com.model.USERTYPE;
import com.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class SignupController {

    @FXML
    private TextField fullNameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField phoneField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private CheckBox agreeCheckBox;

    @FXML
    private VBox errorBox;

    @FXML
    private Label lbl_error;

    @FXML
    private void btnSignupClicked() throws IOException {
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String password = passwordField.getText();

        if (fullName.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
            showError("Please fill in every field.");
            return;
        }

        if (!agreeCheckBox.isSelected()) {
            showError("Please agree to the Terms and Privacy notice.");
            return;
        }

        // Split "Elena Morales" into first and last name for the model
        String firstName = fullName;
        String lastName = "";
        int space = fullName.indexOf(" ");
        if (space > 0) {
            firstName = fullName.substring(0, space);
            lastName = fullName.substring(space + 1);
        }

        // The email is used as the username. The prototype doesn't ask for a
        // date of birth, so today's date is saved as a placeholder.
        User user = ReliefApplication.getInstance().createAccount(
                email, password, firstName, lastName, LocalDate.now(),
                "", email, phone, USERTYPE.RESIDENT);

        if (user == null) {
            showError("An account with that email already exists.");
            return;
        }

        App.setRoot("login");
    }

    @FXML
    private void goToLogin() throws IOException {
        App.setRoot("login");
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