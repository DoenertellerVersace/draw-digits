module org.htw.drawningnumbers {
    requires javafx.controls;
    requires javafx.fxml;
            
        requires org.controlsfx.controls;
                            
    opens org.htw.drawningnumbers to javafx.fxml;
    exports org.htw.drawningnumbers;
}