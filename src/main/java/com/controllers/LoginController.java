package com.controllers;

import java.io.IOException;

import com.hurricane.App;
import com.model.User;
import com.model.UserDirectory;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

/**
 * Handles login validation, password visibility,
 * and the authentication error display.
 */
public class LoginController {

    @FXML
    private StackPane loginRoot;

    @FXML
    private Label titleLabel;

    @FXML
    private HBox errorBanner;

    @FXML
    private Label passwordErrorLabel;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField visiblePasswordField;

    @FXML
    private Button showPasswordButton;

    @FXML
    private CheckBox keepSignedInCheckBox;

    private String passwordFieldStyle;
    private String visiblePasswordFieldStyle;

    @FXML
    private void initialize() {
        // Remember the normal inline styles from the FXML.
        passwordFieldStyle = passwordField.getStyle();
        visiblePasswordFieldStyle = visiblePasswordField.getStyle();

        // Keep the hidden and revealed password values synchronized.
        visiblePasswordField.textProperty()
                .bindBidirectional(passwordField.textProperty());

        setPasswordVisible(false);
        showAuthenticationError(false);

        // Prevent the cursor from starting in the email field.
        Platform.runLater(() -> loginRoot.requestFocus());
    }

    @FXML
    private void togglePasswordVisibility() {
        boolean show = !visiblePasswordField.isVisible();

        TextField previousField =
                show ? passwordField : visiblePasswordField;

        int caretPosition = previousField.getCaretPosition();

        setPasswordVisible(show);

        TextField activeField =
                show ? visiblePasswordField : passwordField;

        activeField.requestFocus();
        activeField.positionCaret(
                Math.min(caretPosition, activeField.getLength())
        );
    }

    private void setPasswordVisible(boolean show) {
        passwordField.setVisible(!show);
        passwordField.setManaged(!show);

        visiblePasswordField.setVisible(show);
        visiblePasswordField.setManaged(show);

        showPasswordButton.setAccessibleText(
                show ? "Hide password" : "Show password"
        );
    }

    private void showAuthenticationError(boolean show) {
        errorBanner.setVisible(show);
        errorBanner.setManaged(show);

        passwordErrorLabel.setVisible(show);
        passwordErrorLabel.setManaged(show);

        titleLabel.setText(
                show ? "Welcome to Hurricane Tracker" : "Log in"
        );

        keepSignedInCheckBox.setText(
                show
                        ? "Keep me signed in"
                        : "Keep me signed in on this device."
        );

        // A later inline declaration overrides the normal border color.
        String errorBorder =
                show ? " -fx-border-color: #B33535;" : "";

        passwordField.setStyle(
                passwordFieldStyle + errorBorder
        );

        visiblePasswordField.setStyle(
                visiblePasswordFieldStyle + errorBorder
        );
    }

    @FXML
    private void btnLoginClicked() throws IOException {
        String usernameOrEmail = usernameField.getText().trim();
        String password = passwordField.getText();

        if (usernameOrEmail.isEmpty() || password.isEmpty()) {
            setPasswordVisible(false);
            showAuthenticationError(true);
            return;
        }

        UserDirectory directory = UserDirectory.getInstance();
        User user = directory.getUser(usernameOrEmail, password);

        if (user == null) {
            setPasswordVisible(false);
            showAuthenticationError(true);
            passwordField.requestFocus();
            return;
        }

        showAuthenticationError(false);

        // Add your application's session handling here if required.
        App.setRoot("user_home");
    }

    @FXML
    private void back() throws IOException {
        App.setRoot("splashscreen");
    }

    @FXML
    private void continueAsGuest() throws IOException {
        // Replace this destination if your guest screen has another name.
        App.setRoot("hurricanerelief");
    }

    @FXML
    private void forgotPassword() throws IOException {
        if (App.class.getResource("forgotpassword.fxml") != null) {
            App.setRoot("forgotpassword");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Password recovery");
        alert.setHeaderText("Password recovery is not available yet");
        alert.setContentText(
                "Connect your password reset screen and recovery "
                + "service to enable this feature."
        );

        if (loginRoot.getScene() != null
                && loginRoot.getScene().getWindow() != null) {
            alert.initOwner(loginRoot.getScene().getWindow());
        }

        alert.showAndWait();
    }
}