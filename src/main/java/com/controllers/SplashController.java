package com.controllers;

import java.io.IOException;
import com.hurricane.App;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;

public class SplashController {

    @FXML
    private VBox splashRoot;

    @FXML
    private void initialize() {
        Platform.runLater(() -> splashRoot.requestFocus());
    }

    @FXML
private void btnLoginClicked() throws IOException {
    App.setRoot("login");
}

@FXML
private void btnCreateAccountClicked() throws IOException {
    App.setRoot("signup");
}

@FXML
private void btnGuestClicked() throws IOException {
    App.setRoot("login");
}
}