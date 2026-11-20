package com.controllers;

import java.util.ArrayList;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class RequestCards {

    // Sample data for now. Swap this for real requests later.
    public static ArrayList<String[]> getSampleRequests() {
        ArrayList<String[]> requests = new ArrayList<>();
        requests.add(new String[] {"Food & water", "3 people · ZIP 33607", "12 min drive · Needed today"});
        requests.add(new String[] {"Supplies", "4 people · ZIP 33609", "8 min drive · Needed today"});
        requests.add(new String[] {"Transport", "2 people · ZIP 33607", "16 min drive · Needed today"});
        return requests;
    }

    public static VBox buildCard(String title, String line1, String line2) {
        Label tag = new Label("Matches your skill");
        tag.getStyleClass().add("tag");

        Label name = new Label(title);
        name.getStyleClass().add("card-title");

        Label first = new Label(line1);
        first.getStyleClass().add("subtitle");

        Label second = new Label(line2);
        second.getStyleClass().add("subtitle");

        Button preview = new Button("Preview request");
        preview.getStyleClass().add("secondary-button");

        VBox card = new VBox(tag, name, first, second, preview);
        card.getStyleClass().add("card");
        return card;
    }
}