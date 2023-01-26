module org.htw.drawdigits {
  requires java.desktop;
  requires javafx.controls;
  requires javafx.fxml;
  requires javafx.swing;
  requires org.controlsfx.controls;
  requires org.json;
  requires okhttp3;

  opens org.htw.drawdigits to javafx.fxml;
  exports org.htw.drawdigits;
}