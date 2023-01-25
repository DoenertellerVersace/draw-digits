module org.htw.drawningnumbers {
  requires java.desktop;
  requires javafx.controls;
  requires javafx.fxml;
  requires javafx.swing;
  requires org.controlsfx.controls;
  requires org.json;
  requires okhttp3;

  opens org.htw.drawingnumbers to javafx.fxml;
  exports org.htw.drawingnumbers;
}