package com.controllers;

import java.io.IOException;
import com.hurricane.App;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

/**
 * Controls the password recovery email screen.
 */
public class ForgotPasswordController {

    @FXML
    private StackPane forgotPasswordRoot;

    @FXML
    private TextField emailField;

    @FXML
    private Label emailErrorLabel;

    private String normalEmailStyle;

    @FXML
    private void initialize() {
        normalEmailStyle = emailField.getStyle();

        emailField.textProperty().addListener(
                (observable, oldValue, newValue) -> {
                    emailErrorLabel.setVisible(false);
                    emailErrorLabel.setManaged(false);
                    emailField.setStyle(normalEmailStyle);
                }
        );

        Platform.runLater(
                () -> forgotPasswordRoot.requestFocus()
        );
    }

    @FXML
    private void sendResetLink() throws IOException {
        String email = emailField.getText().trim();

        // Basic format checking, not account verification.
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            emailErrorLabel.setVisible(true);
            emailErrorLabel.setManaged(true);

            emailField.setStyle(
                    normalEmailStyle
                    + " -fx-border-color: #B33535;"
            );

            emailField.requestFocus();
            return;
        }

        /*
         * Prototype navigation only.
         * Connect your password recovery service here before
         * using this flow to send real reset emails.
         */
        CheckEmailController.setRecoveryEmail(email);
        App.setRoot("checkemail");
    }

    @FXML
    private void returnToLogin() throws IOException {
        CheckEmailController.clearRecoveryEmail();
        App.setRoot("login");
    }
}