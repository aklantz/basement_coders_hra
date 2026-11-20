package com.controllers;

import java.io.IOException;
import com.hurricane.App;
import javafx.fxml.FXML;

public class VolunteerCantCompleteController {

    @FXML
    private void btnReturnClicked() throws IOException {
        App.setRoot("volunteer_tasks");
    }

    @FXML
    private void btnKeepClicked() throws IOException {
        App.setRoot("volunteer_task_accepted");
    }
}