package com.controllers;

import java.io.IOException;
import com.hurricane.App;
import com.model.User;
import com.model.UserDirectory;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class LoginController {

    @FXML
    private VBox loginRoot;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField visiblePasswordField;

    @FXML
    private Button showPasswordButton;

    @FXML
    private Label lbl_error;

    @FXML
    private void initialize() {
        visiblePasswordField.textProperty()
                .bindBidirectional(passwordField.textProperty());

        passwordField.setVisible(true);
        passwordField.setManaged(true);

        visiblePasswordField.setVisible(false);
        visiblePasswordField.setManaged(false);

        Platform.runLater(() -> loginRoot.requestFocus());
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
    private void btnLoginClicked() throws IOException {
        String username = usernameField.getText();
        String password = passwordField.getText();

        UserDirectory directory = UserDirectory.getInstance();
        User user = directory.getUser(username, password);

        if (user == null) {
            lbl_error.setText("Invalid login credentials.");
            lbl_error.setManaged(true);
            lbl_error.setVisible(true);
            return;
        }

        App.setRoot("user_home");
    }

    @FXML
    private void back() throws IOException {
        App.setRoot("home");
    }
}