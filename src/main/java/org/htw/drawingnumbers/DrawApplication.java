package org.htw.drawningnumbers;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class DrawApplication extends Application {
  public static void main(String[] args) {
    launch();
  }

  @Override
  public void start(Stage stage) throws IOException {
    FXMLLoader fxmlLoader = new FXMLLoader(new File("src/main/resources/org/htw/drawningnumbers/primary.fxml").toURI().toURL());
    Scene scene = new Scene(fxmlLoader.load(), 1000, 520);
    PrimaryController controller = fxmlLoader.getController();
    controller.setStage(stage);
    controller.cleanCanvas();
    controller.drawResult();
    stage.setTitle("Hello!");
    stage.setScene(scene);
    stage.show();
  }
}