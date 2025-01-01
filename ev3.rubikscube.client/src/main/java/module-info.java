module ev3.rubikscube.ui {
    opens ev3.rubikscube.ui to javafx.graphics, javafx.fxml, javafx.controls, javafx.swing;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.swing;
    requires io.vertx.core;
    requires opencv;
}