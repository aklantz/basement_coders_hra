package com.controllers;

import java.io.IOException;
import javafx.fxml.FXML;
import com.hurricane.App;

public class HurricaneReliefController {

    @FXML
    private void switchToLogin() throws IOException {
        App.setRoot("login");
    }
}