module com.example.front {
    requires javafx.controls;
    requires javafx.fxml;
    requires kotlin.stdlib;

    requires org.kordamp.bootstrapfx.core;

    opens com.example.front to javafx.fxml;
    exports com.example.front;
}