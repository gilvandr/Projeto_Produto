module org.p1produto {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.p1produto to javafx.fxml;
    exports org.p1produto;
}