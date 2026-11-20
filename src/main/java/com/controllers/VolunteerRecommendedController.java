package com.controllers;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;

public class VolunteerRecommendedController {

    @FXML
    private VolunteerNavController volunteerNavController;

    @FXML
    private VBox recommendedList;

    @FXML
    private void initialize() {
        volunteerNavController.setActive("tasks");

        for (String[] request : RequestCards.getSampleRequests()) {
            recommendedList.getChildren().add(
                    RequestCards.buildCard(request[0], request[1], request[2]));
        }
    }
}