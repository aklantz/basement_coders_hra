package com.controllers;

import java.io.IOException;
import java.util.ArrayList;
import com.hurricane.App;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class VolunteerCompleteController {

    @FXML
    private VBox itemList;

    @FXML
    private VBox errorBox;

    @FXML
    private Label lbl_error;

    private ArrayList<ToggleButton> itemButtons = new ArrayList<>();

    @FXML
    private void initialize() {
        String[][] items = {
            {"Drinking water", "6 gallons"},
            {"Ready-to-eat food", "3 household meal packs"},
            {"Portable phone charger", "1 charged power bank"}
        };

        for (String[] item : items) {
            Label name = new Label(item[0]);
            name.getStyleClass().add("field-label");
            Label detail = new Label(item[1]);
            detail.getStyleClass().add("hint");

            ToggleButton button = new ToggleButton();
            button.setGraphic(new VBox(4, name, detail));
            button.getStyleClass().add("skill-button");
            button.setMaxWidth(Double.MAX_VALUE);
            button.setSelected(true);

            Label check = new Label("✓");
            check.getStyleClass().add("skill-check");
            check.setMouseTransparent(true);
            check.visibleProperty().bind(button.selectedProperty());
            StackPane.setAlignment(check, Pos.CENTER_RIGHT);
            StackPane.setMargin(check, new Insets(0, 16, 0, 0));

            itemList.getChildren().add(new StackPane(button, check));
            itemButtons.add(button);
        }
    }

    @FXML
    private void btnSubmitClicked() throws IOException {
        boolean anySelected = false;
        for (ToggleButton button : itemButtons) {
            if (button.isSelected()) {
                anySelected = true;
            }
        }

        if (!anySelected) {
            lbl_error.setText("Select at least one delivered item.");
            errorBox.setManaged(true);
            errorBox.setVisible(true);
            return;
        }

        // The note and items are not saved yet.
        App.setRoot("volunteer_submitted");
    }

    @FXML
    private void back() throws IOException {
        App.setRoot("volunteer_directions");
    }
}