package com.controllers;

import java.io.IOException;
import javafx.fxml.FXML;
import com.hurricane.App;

public class LoginController {

    @FXML
    private void switchToHurricaneRelief() throws IOException {
        App.setRoot("HurricaneRelief");
    }
}
