module com.hurricane {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;
    requires javafx.web;

    opens com.hurricane to javafx.fxml;
    exports com.hurricane;
}