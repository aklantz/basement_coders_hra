package com.controllers;

import java.io.IOException;
import com.hurricane.App;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

/**
 * Displays the email entered during password recovery.
 */
public class CheckEmailController {

    // Temporarily passes the address between these two controllers.
    private static String recoveryEmail = "";

    @FXML
    private StackPane checkEmailRoot;

    @FXML
    private Label emailAddressLabel;

    public static void setRecoveryEmail(String email) {
        recoveryEmail = email == null ? "" : email;
    }

    public static void clearRecoveryEmail() {
        recoveryEmail = "";
    }

    @FXML
    private void initialize() {
        emailAddressLabel.setText(
                recoveryEmail.isEmpty()
                        ? "Your email address"
                        : recoveryEmail
        );

        Platform.runLater(
                () -> checkEmailRoot.requestFocus()
        );
    }

    @FXML
    private void returnToLogin() throws IOException {
        clearRecoveryEmail();
        App.setRoot("login");
    }

    @FXML
    private void backToForgotPassword() throws IOException {
        clearRecoveryEmail();
        App.setRoot("forgotpassword");
    }

    @FXML
    private void resendResetLink() {
        /*
         * Connect the same password recovery service used by
         * ForgotPasswordController here.
         */
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Password recovery");
        alert.setHeaderText("Email delivery is not connected yet");
        alert.setContentText(
                "This prototype displays the recovery screens, "
                + "but no password reset email has been sent."
        );

        if (checkEmailRoot.getScene() != null
                && checkEmailRoot.getScene().getWindow() != null) {
            alert.initOwner(
                    checkEmailRoot.getScene().getWindow()
            );
        }

        alert.showAndWait();
    }
}