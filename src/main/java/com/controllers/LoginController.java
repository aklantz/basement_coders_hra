package com.controllers;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import com.hurricane.App;
import com.model.User;
import com.model.UserDirectory;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

public class LoginController implements Initializable {

    @FXML
    private TextField txt_username;
    @FXML
    private PasswordField txt_password;
    @FXML
    private Label lbl_error;

    @FXML
    private void btnLoginClicked(MouseEvent event) throws IOException {
        String username = txt_username.getText();
        String password = txt_password.getText();

        UserDirectory directory = UserDirectory.getInstance();
User user = directory.getUser(username, password);

        if (user == null) {
            lbl_error.setText("Invalid login credentials.");
            return;
        }

        App.setRoot("user_home");
    }

    @FXML
    private void switchToHurricaneRelief() throws IOException {
        App.setRoot("hurricanerelief");
    }

    @FXML
    private void back(MouseEvent event) throws IOException {
        App.setRoot("home");
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }
}