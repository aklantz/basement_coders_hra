package com.controllers;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import com.hurricane.App;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField visiblePasswordField;

    @FXML
    private Button showPasswordButton;

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

        showPasswordButton.setAccessibleText(
                showPassword ? "Hide password" : "Show password"
        );

        TextField activeField =
                showPassword ? visiblePasswordField : passwordField;

        activeField.requestFocus();
        activeField.positionCaret(activeField.getText().length());
    }

    @FXML
    private void switchToHurricaneRelief() throws IOException {
        App.setRoot("hurricanerelief");
    }
}