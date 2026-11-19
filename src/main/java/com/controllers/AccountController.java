package com.controllers;

import com.hurricane.App;
import com.model.Household;
import com.model.HouseholdMember;
import com.model.ReliefApplication;
import com.model.ResidentAccount;
import com.model.User;
import java.io.IOException;
import java.util.Properties;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Controls account information, households and local preferences.
 */
public class AccountController {

    private static final String BASE_STYLE =
            "hra.accessibility.baseStyle";

    private static final Pattern FONT_SIZE =
            Pattern.compile("-fx-font-size:\\s*([0-9.]+)px");

    @FXML private StackPane accountRoot;
    @FXML private BorderPane mainContent;
    @FXML private ScrollPane pageScroll;

    @FXML private Label locationLabel;
    @FXML private Label nameLabel;
    @FXML private Label roleLabel;
    @FXML private Label emailLabel;
    @FXML private Label phoneLabel;
    @FXML private Label messageLabel;

    @FXML private VBox profilePanel;
    @FXML private VBox householdPanel;
    @FXML private VBox householdList;
    @FXML private VBox alertsPanel;
    @FXML private VBox accessibilityPanel;
    @FXML private VBox offlinePanel;

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField addressField;

    @FXML private Button primaryLocationButton;

    @FXML private TextField householdNameField;
    @FXML private TextField memberNameField;
    @FXML private TextField memberAgeField;

    @FXML private ComboBox<String> householdChoice;
    @FXML private ComboBox<String> textSizeChoice;

    @FXML private CheckBox reducedMotion;
    @FXML private TextArea offlineNotes;

    private UUID ownerId;
    private Properties settings = new Properties();

    @FXML
    private void initialize() {
        User user = currentUser();

        if (user == null || user.getUserId() == null) {
            mainContent.setVisible(false);
            Platform.runLater(() -> navigate("login"));
            return;
        }

        ownerId = user.getUserId();

        textSizeChoice.getItems().setAll(
                "Standard",
                "Large",
                "Extra large"
        );

        try {
            settings = ResidentAccount.loadSettings(
                    ResidentAccount.settingsPath(ownerId)
            );
        } catch (IOException
                | IllegalArgumentException
                | SecurityException exception) {
            message(
                    "Saved preferences could not be read. "
                    + "Profile and request data are unchanged."
            );
        }

        String textSize = settings.getProperty(
                "textSize",
                "Standard"
        );

        textSizeChoice.setValue(
                textSizeChoice.getItems().contains(textSize)
                        ? textSize
                        : "Standard"
        );

        reducedMotion.setSelected(
                Boolean.parseBoolean(
                        settings.getProperty(
                                "reducedMotion",
                                "true"
                        )
                )
        );

        offlineNotes.setText(
                settings.getProperty("offlineNotes", "")
        );

        refreshProfile();
        refreshHouseholds();

        Platform.runLater(() -> accountRoot.requestFocus());
    }

    private boolean session() {
        User user = currentUser();

        if (user != null
                && ownerId != null
                && ownerId.equals(user.getUserId())) {
            return true;
        }

        mainContent.setVisible(false);
        navigate("login");
        return false;
    }

    private void refreshProfile() {
        User user = currentUser();

        nameLabel.setText(
                value(user.getFullName(), "Your account")
        );

        roleLabel.setText(
                user.getType()
                    .name()
                    .replace('_', ' ')
                    .toLowerCase(java.util.Locale.ROOT)
                + " account"
        );

        emailLabel.setText(
                value(user.getEmailAddress(), "Email not recorded")
        );

        phoneLabel.setText(
                value(user.getPhoneNumber(), "Phone not recorded")
        );

        locationLabel.setText(
                value(user.getHomeAddress(), "Location not set")
        );

        primaryLocationButton.setText(
                value(
                        user.getHomeAddress(),
                        "Set your primary location"
                )
        );
    }

    @FXML
    private void editProfile() {
        if (!session()) {
            return;
        }

        User user = currentUser();

        nameField.setText(value(user.getFullName(), ""));
        emailField.setText(value(user.getEmailAddress(), ""));
        phoneField.setText(value(user.getPhoneNumber(), ""));
        addressField.setText(value(user.getHomeAddress(), ""));

        shown(profilePanel, true);
        nameField.requestFocus();
    }

    @FXML
    private void editLocation() {
        editProfile();

        if (session()) {
            addressField.requestFocus();
        }
    }

    @FXML
    private void closeProfile() {
        shown(profilePanel, false);
    }

    @FXML
    private void saveProfile() {
        if (!session()) {
            return;
        }

        if (nameField.getText().trim().isEmpty()
                || !ResidentAccount.validEmail(
                        emailField.getText()
                )
                || ResidentAccount.phoneDigits(
                        phoneField.getText()
                ) == null) {
            message(
                    "Enter your full name, a valid email address "
                    + "and a 10-digit US phone number."
            );
            return;
        }

        boolean saved =
                ReliefApplication.getInstance().updateMyProfile(
                        nameField.getText(),
                        addressField.getText(),
                        emailField.getText(),
                        phoneField.getText()
                );

        if (!saved) {
            message(
                    "Your profile was not saved. "
                    + "The email may already be in use, or saving failed. "
                    + "Your entries are still here."
            );
            return;
        }

        refreshProfile();
        shown(profilePanel, false);
        message("Profile saved.");
    }

    @FXML
    private void manageHousehold() {
        if (!session()) {
            return;
        }

        refreshHouseholds();

        shown(
                householdPanel,
                !householdPanel.isVisible()
        );
    }

    private void refreshHouseholds() {
        String previous = householdChoice.getValue();

        householdList.getChildren().clear();
        householdChoice.getItems().clear();

        for (Household household
                : ReliefApplication.getInstance().getHouseholds()) {
            if (household == null
                    || !ownerId.equals(
                            household.getHouseholdOwnerUUID()
                    )) {
                continue;
            }

            householdChoice.getItems().add(
                    household.getHouseholdName()
            );

            Label heading = new Label(
                    household.getHouseholdName()
            );

            heading.setStyle(
                    "-fx-font-size: 15px;"
                    + "-fx-font-weight: bold;"
                    + "-fx-text-fill: #142D45;"
            );

            heading.setWrapText(true);
            heading.setMinHeight(Region.USE_PREF_SIZE);

            householdList.getChildren().add(heading);

            if (household.getMembers().isEmpty()) {
                householdList.getChildren().add(
                        wrapped("No members added yet.")
                );
            }

            for (HouseholdMember member
                    : household.getMembers()) {
                householdList.getChildren().add(
                        wrapped(
                                member.getName()
                                + (member.isPet()
                                        ? " · " + member.getPetType()
                                        : " · age " + member.getAge())
                                + " · " + member.getSafetyStatus()
                        )
                );
            }
        }

        if (householdChoice.getItems().isEmpty()) {
            householdList.getChildren().add(
                    wrapped("Create your first household below.")
            );
        }

        if (householdChoice.getItems().contains(previous)) {
            householdChoice.setValue(previous);
        } else if (!householdChoice.getItems().isEmpty()) {
            householdChoice.setValue(
                    householdChoice.getItems().get(0)
            );
        }

        applyTextScale(accountRoot);
    }

    @FXML
    private void createHousehold() {
        if (!session()) {
            return;
        }

        boolean saved =
                ReliefApplication.getInstance().createMyHousehold(
                        householdNameField.getText()
                );

        if (!saved) {
            message(
                    "Household not saved. "
                    + "Enter a new household name and try again."
            );
            return;
        }

        householdNameField.clear();
        refreshHouseholds();
        message("Household created.");
    }

    @FXML
    private void addMember() {
        if (!session()) {
            return;
        }

        int age;

        try {
            age = Integer.parseInt(
                    memberAgeField.getText().trim()
            );
        } catch (NumberFormatException exception) {
            message(
                    "Enter an age as a whole number from 0 to 130."
            );
            return;
        }

        boolean saved =
                ReliefApplication.getInstance().addMyHouseholdMember(
                        householdChoice.getValue(),
                        memberNameField.getText(),
                        age
                );

        if (!saved) {
            message(
                    "Member not saved. Check the household, name and age; "
                    + "duplicate member names are not allowed."
            );
            return;
        }

        memberNameField.clear();
        memberAgeField.setText("0");

        refreshHouseholds();
        message("Household member added.");
    }

    @FXML
    private void showAlerts() {
        if (session()) {
            shown(alertsPanel, !alertsPanel.isVisible());
        }
    }

    @FXML
    private void showAccessibility() {
        if (session()) {
            shown(
                    accessibilityPanel,
                    !accessibilityPanel.isVisible()
            );
        }
    }

    @FXML
    private void saveAccessibility() {
        if (!session()) {
            return;
        }

        Properties updated = new Properties();
        updated.putAll(settings);

        updated.setProperty(
                "textSize",
                value(textSizeChoice.getValue(), "Standard")
        );

        updated.setProperty(
                "reducedMotion",
                String.valueOf(reducedMotion.isSelected())
        );

        if (saveSettings(updated)) {
            applyTextScale(accountRoot);

            message(
                    "Accessibility preferences saved on this device."
            );
        }
    }

    @FXML
    private void showOffline() {
        if (session()) {
            shown(offlinePanel, !offlinePanel.isVisible());
        }
    }

    @FXML
    private void saveOffline() {
        if (!session()) {
            return;
        }

        Properties updated = new Properties();
        updated.putAll(settings);

        updated.setProperty(
                "offlineNotes",
                offlineNotes.getText()
        );

        if (saveSettings(updated)) {
            message(
                    "Your safety notes are saved offline on this device."
            );
        }
    }

    private boolean saveSettings(Properties updated) {
        try {
            ResidentAccount.saveSettings(
                    ResidentAccount.settingsPath(ownerId),
                    updated
            );

            settings = updated;
            return true;

        } catch (IOException | SecurityException exception) {
            message(
                    "Preferences were not saved. "
                    + "Your entries are still here."
            );
            return false;
        }
    }

    @FXML
    private void signOut() {
        if (session()) {
            ReliefApplication.getInstance().logout();
            navigate("splashscreen");
        }
    }

    /**
     * Applies the signed-in user's saved text size.
     *
     * @param root the screen root
     */
    public static void applyTextScale(Parent root) {
        User user = ReliefApplication.getInstance().getCurrentUser();

        if (user == null || user.getUserId() == null) {
            return;
        }

        double factor = 1;

        try {
            Properties preferences =
                    ResidentAccount.loadSettings(
                            ResidentAccount.settingsPath(
                                    user.getUserId()
                            )
                    );

            String size = preferences.getProperty(
                    "textSize",
                    "Standard"
            );

            factor = size.equals("Large")
                    ? 1.15
                    : size.equals("Extra large") ? 1.3 : 1;

        } catch (IOException
                | IllegalArgumentException
                | SecurityException exception) {
            // Use the normal size if preferences cannot be read.
        }

        scaleNode(root, factor);
    }

    private static void scaleNode(
            Node node,
            double factor
    ) {
        String current = node.getStyle();

        String previousApplied = (String) node.getProperties().get(
                BASE_STYLE + ".applied"
        );

        String base = (String) node.getProperties().get(
                BASE_STYLE
        );

        if (base == null
                || previousApplied == null
                || !previousApplied.equals(current)) {
            base = current;
            node.getProperties().put(BASE_STYLE, base);
        }

        Matcher matcher = FONT_SIZE.matcher(base);
        StringBuffer output = new StringBuffer();

        while (matcher.find()) {
            matcher.appendReplacement(
                    output,
                    Matcher.quoteReplacement(
                            "-fx-font-size: "
                            + Double.parseDouble(matcher.group(1))
                              * factor
                            + "px"
                    )
            );
        }

        matcher.appendTail(output);

        String applied = output.toString();

        node.setStyle(applied);

        node.getProperties().put(
                BASE_STYLE + ".applied",
                applied
        );

        if (node instanceof Parent) {
            for (Node child
                    : ((Parent) node).getChildrenUnmodifiable()) {
                scaleNode(child, factor);
            }
        }

        if (node instanceof ScrollPane) {
            Node content = ((ScrollPane) node).getContent();

            if (content != null) {
                scaleNode(content, factor);
            }
        }
    }

    private Label wrapped(String text) {
        Label label = new Label(text);

        label.setWrapText(true);
        label.setMinHeight(Region.USE_PREF_SIZE);

        label.setStyle(
                "-fx-font-size: 13px;"
                + "-fx-text-fill: #587083;"
        );

        return label;
    }

    private void message(String text) {
        messageLabel.setText(text);
        shown(messageLabel, true);
        pageScroll.setVvalue(0);
    }

    private static void shown(
            Node node,
            boolean visible
    ) {
        node.setVisible(visible);
        node.setManaged(visible);
    }

    private static String value(
            String value,
            String fallback
    ) {
        return value == null || value.trim().isEmpty()
                ? fallback
                : value;
    }

    private User currentUser() {
        return ReliefApplication.getInstance().getCurrentUser();
    }

    private void navigate(String screen) {
        try {
            App.setRoot(screen);
        } catch (IOException exception) {
            message(
                    "The " + screen + " screen could not be opened."
            );
        }
    }

    @FXML
    private void openHome() {
        if (session()) {
            navigate("user_home");
        }
    }

    @FXML
    private void openRequests() {
        if (session()) {
            navigate("my_requests");
        }
    }

    @FXML
    private void openSafety() {
        if (session()) {
            navigate("safety");
        }
    }

    @FXML
    private void openAccount() {
        if (session()) {
            refreshProfile();
            message("Your account is up to date.");
        }
    }
}