package com.controllers;

import java.io.IOException;
import com.hurricane.App;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;

/**
 * Controls the email confirmation screen and its resend popup.
 */
public class CheckEmailController {

    private static String recoveryEmail = "";

    @FXML
    private StackPane checkEmailRoot;

    @FXML
    private ScrollPane checkEmailContent;

    @FXML
    private Label emailAddressLabel;

    @FXML
    private StackPane resendOverlay;

    @FXML
    private Button resendButton;

    @FXML
    private Button resendDoneButton;

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

        resendOverlay.setVisible(false);
        resendOverlay.setManaged(false);

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
         * Prototype confirmation only.
         * Connect your recovery service here to send a real email.
         */

        // Prevent interaction with the screen behind the popup.
        checkEmailContent.setDisable(true);

        resendOverlay.setManaged(true);
        resendOverlay.setVisible(true);
        resendOverlay.toFront();

        resendDoneButton.requestFocus();
    }

    @FXML
    private void closeResendPopup() {
        resendOverlay.setVisible(false);
        resendOverlay.setManaged(false);

        checkEmailContent.setDisable(false);
        resendButton.requestFocus();
    }

    @FXML
    private void handleOverlayKey(KeyEvent event) {
        if (event.getCode() == KeyCode.ESCAPE) {
            closeResendPopup();
            event.consume();
        }
    }
}