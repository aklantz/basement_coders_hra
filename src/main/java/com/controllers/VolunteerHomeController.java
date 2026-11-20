package com.controllers;

import java.io.IOException;
import com.hurricane.App;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;

public class VolunteerHomeController {

    @FXML
    private VolunteerNavController volunteerNavController;

    @FXML
    private VBox recommendedList;

    @FXML
    private void initialize() {
        volunteerNavController.setActive("home");

        String[] first = RequestCards.getSampleRequests().get(0);
        recommendedList.getChildren().add(
                RequestCards.buildCard(first[0], first[1], first[2]));
    }

    @FXML
    private void btnViewAllClicked() throws IOException {
        App.setRoot("volunteer_recommended");
    }
}