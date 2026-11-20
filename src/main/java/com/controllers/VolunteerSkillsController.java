package com.controllers;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import com.hurricane.App;
import com.model.ReliefApplication;
import com.model.USERTYPE;
import com.model.User;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class VolunteerSkillsController {

    @FXML
    private VBox skillList;

    @FXML
    private VBox errorBox;

    @FXML
    private Label lbl_error;

    private ArrayList<ToggleButton> skillButtons = new ArrayList<>();

    @FXML
    private void initialize() {
        String[] skills = {"Food", "Water", "Medical", "Rescue", "Transport",
                "Supplies", "Power", "Debris", "Other"};

        for (String skill : skills) {
            ToggleButton button = new ToggleButton(skill);
            button.getStyleClass().add("skill-button");
            button.setMaxWidth(Double.MAX_VALUE);

            Label check = new Label("✓");
            check.getStyleClass().add("skill-check");
            check.setMouseTransparent(true);
            check.visibleProperty().bind(button.selectedProperty());
            StackPane.setAlignment(check, Pos.CENTER_RIGHT);
            StackPane.setMargin(check, new Insets(0, 16, 0, 0));

            skillList.getChildren().add(new StackPane(button, check));
            skillButtons.add(button);
        }
    }

    @FXML
    private void btnSubmitClicked() throws IOException {
        if (VolunteerSignupData.email == null) {
            showError("Please start from the volunteer sign up page.");
            return;
        }

        ArrayList<String> chosen = new ArrayList<>();
        for (ToggleButton button : skillButtons) {
            if (button.isSelected()) {
                chosen.add(button.getText());
            }
        }

        if (chosen.isEmpty()) {
            showError("Choose at least one skill.");
            return;
        }

        VolunteerSignupData.skills = chosen;

        String fullName = VolunteerSignupData.fullName;
        String firstName = fullName;
        String lastName = "";
        int space = fullName.indexOf(" ");
        if (space > 0) {
            firstName = fullName.substring(0, space);
            lastName = fullName.substring(space + 1);
        }

        // The ZIP codes go in the home address slot for now.
        // The prototype doesn't ask for a date of birth, so today is a placeholder.
        User user = ReliefApplication.getInstance().createAccount(
                VolunteerSignupData.email, VolunteerSignupData.password,
                firstName, lastName, LocalDate.now(),
                VolunteerSignupData.zipCodes, VolunteerSignupData.email,
                VolunteerSignupData.phone, USERTYPE.VOLUNTEER);

        if (user == null) {
            showError("An account with that email already exists.");
            return;
        }

        App.setRoot("volunteer_pending");
    }

    @FXML
    private void back() throws IOException {
        App.setRoot("volunteer_signup");
    }

    private void showError(String message) {
        lbl_error.setText(message);
        errorBox.setManaged(true);
        errorBox.setVisible(true);
    }
}