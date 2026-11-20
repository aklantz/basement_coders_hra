package com.controllers;

import java.io.IOException;
import com.hurricane.App;
import javafx.fxml.FXML;

public class VolunteerSubmittedController {

    @FXML
    private void btnHomeClicked() throws IOException {
        App.setRoot("volunteer_home");
    }

    @FXML
    private void btnBrowseClicked() throws IOException {
        App.setRoot("volunteer_recommended");
    }
}