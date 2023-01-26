package org.htw.drawdigits;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class DrawApplication extends Application {

  public static final File CONTROLLER_FXML =
      new File("src/main/resources/org/htw/drawdigits/primary.fxml");

  public static void main(String[] args) {
    launch();
  }

  @Override
  public void start(Stage stage) throws IOException {
    FXMLLoader fxmlLoader = new FXMLLoader(CONTROLLER_FXML.toURI().toURL());
    Scene scene = new Scene(fxmlLoader.load(), 800, 520);
    PrimaryController controller = fxmlLoader.getController();
    controller.setStage(stage);
    controller.cleanCanvas();
    controller.drawResult();
    stage.setTitle("Start drawing digits today!");
    stage.setScene(scene);
    stage.show();
  }
}