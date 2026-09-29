module com.hurricane {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.hurricane to javafx.fxml;
    exports com.hurricane;
}
