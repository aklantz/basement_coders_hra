module com.hurricane {
    requires transitive javafx.controls;
    requires javafx.fxml;
    requires json.simple;

    opens com.hurricane to javafx.fxml;
    exports com.hurricane;

    opens com.model to javafx.fxml;
    exports com.model;
}
