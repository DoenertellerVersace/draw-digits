package org.htw.drawingnumbers;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.util.Pair;
import okhttp3.*;
import org.json.JSONObject;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.*;

import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static javafx.scene.paint.Color.BLACK;
import static javafx.scene.paint.Color.WHITE;

public class PrimaryController {

  public static final File MODEL_FILE_INITIAL_DIRECTORY = new File(System.getProperty("user.home"));
  public static final String MODEL_FILE_DESC = "model file (.h5)";
  public static final String MODEL_FILE_EXT = "*.h5";
  public static final String MODEL_BEING_LOADED = "%s is loading...";
  public static final String MODEL_READY = "%s is ready to go!";
  public static final String MODEL_PLS_CHOOSE = "load a model please...";
  public static final String NOTHING_DRAWN_YET = "nothing drawn yet...";
  public static final OkHttpClient OK_HTTP_CLIENT = new OkHttpClient();
  public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
  public static final String URL = "http://localhost:8000/%s";
  public static final int IMG_PROC_W = 28;
  public static final int IMG_PROC_H = 28;
  public static final String RESET_METHOD = "reset";
  public static final String MODEL_METHOD = "model";
  public static final String PREDICT_METHOD = "predict";
  private static final String MODEL_FAILED = "loading %s failed!";
  private LinkedList<Draw> draws = new LinkedList<>();
  private List<Pair<Double, Double>> drawNow;
  private File modelFile;
  private Color background = BLACK;
  private Color brush = WHITE;
  private Stage stage;
  @FXML
  public Canvas resultCanvas;
  @FXML
  private Slider brushSize;
  @FXML
  private Label modelLabel;
  @FXML
  private CheckBox erase;
  @FXML
  private Canvas canvas;

  private static String post(String method, String json) throws IOException {
    RequestBody body = RequestBody.create(json, JSON);
    Request request = new Request.Builder()
        .url(format(URL, method))
        .post(body)
        .build();
    try (Response response = OK_HTTP_CLIENT.newCall(request).execute()) {
      return requireNonNull(response.body()).string();
    } catch (IOException e) {
      System.out.printf("error while performing post: %s", e.getMessage());
      return "";
    }
  }

  private static String get(String method) {
    Request request = new Request.Builder()
        .url(format(URL, method))
        .build();
    try (Response response = OK_HTTP_CLIENT.newCall(request).execute()) {
      return requireNonNull(response.body()).string();
    } catch (IOException e) {
      System.out.printf("error while performing get: %s", e.getMessage());
      return "";
    }
  }

  private static String convertToJSON(String key, String value) throws IOException {
    return new JSONObject(Map.of(key, value)).toString();
  }

  private static String replaceBackSlashes(File file) {
    return file.getAbsolutePath().replace("\\", "/");
  }

  private static BufferedImage getScaledDownImage(BufferedImage image) {
    return toBufferedImage(image.getScaledInstance(IMG_PROC_W, IMG_PROC_H, BufferedImage.SCALE_AREA_AVERAGING));
  }

  public static BufferedImage toBufferedImage(Image img) {
    if (img instanceof BufferedImage) {
      return (BufferedImage) img;
    }
    BufferedImage buffered = new BufferedImage(
        img.getWidth(null), img.getHeight(null),
        BufferedImage.TYPE_INT_ARGB
    );
    Graphics2D g = buffered.createGraphics();
    g.drawImage(img, 0, 0, null);
    g.dispose();
    return buffered;
  }

  private File chooseModelFile() {
    ExtensionFilter filter = new ExtensionFilter(MODEL_FILE_DESC, MODEL_FILE_EXT);
    FileChooser fileChooser = new FileChooser();
    fileChooser.getExtensionFilters().add(filter);
    fileChooser.setInitialDirectory(MODEL_FILE_INITIAL_DIRECTORY);
    return fileChooser.showOpenDialog(stage);
  }

  public void canvasOnMousePressed(MouseEvent mouseEvent) {
    List<Pair<Double, Double>> startPoint = List.of(new Pair<>(mouseEvent.getX(), mouseEvent.getY()));
    drawNow = new LinkedList<>(startPoint);
    draw(getDraw(startPoint));
  }

  public void canvasOnMouseDragged(MouseEvent mouseEvent) {
    Pair<Double, Double> point = new Pair<>(mouseEvent.getX(), mouseEvent.getY());
    drawNow.add(point);
    draw(getDraw(List.of(point)));
  }

  public void canvasOnMouseReleased(MouseEvent mouseEvent) throws IOException {
    drawNow.add(new Pair<>(mouseEvent.getX(), mouseEvent.getY()));
    Draw draw = getDraw(drawNow);
    draws.add(draw);
    draw(draw);
    evalCanvas();
  }

  private Draw getDraw(List<Pair<Double, Double>> curve) {
    return new Draw(curve, brushSize.getValue(),
        erase.isSelected() ? Draw.Paint.BACKGROUND : Draw.Paint.BRUSH
    );
  }

  public void drawResult(String string) {
    GraphicsContext g = resultCanvas.getGraphicsContext2D();
    g.setFill(WHITE);
    g.fillRect(0, 0, 400, 400);
    g.setFill(BLACK);
    g.setTextAlign(TextAlignment.CENTER);
    g.strokeText(string, 200, 200, 400);
  }

  public void drawResult() {
    drawResult(NOTHING_DRAWN_YET);
  }

  private void draw(Draw curve) {
    GraphicsContext g = canvas.getGraphicsContext2D();
    g.setFill(getColor(curve.getPaint()));
    curve.getPoints().forEach(point -> {
      double t = curve.getSize();
      g.fillRect(point.getKey() - t / 2, point.getValue() - t / 2, t, t);
    });
  }

  private void evalCanvas() throws IOException {
    String image = getGreyScaleCSVFromCanvas();
    drawResult(post(PREDICT_METHOD, convertToJSON("data", image)));
  }

  private String getGreyScaleCSVFromCanvas() {
    List<String> headers = new ArrayList<>();
    List<String> values = new ArrayList<>();
    BufferedImage buffered = getScaledDownImage(SwingFXUtils.fromFXImage(canvas.snapshot(null, null), null));
    for (int i = 0; i < buffered.getHeight(); i++) {
      for (int j = 0; j < buffered.getWidth(); j++) {
        headers.add(format("pixel%d", i * 28 + j));
        int rgb = buffered.getRGB(j, i);
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = (rgb & 0xFF);
        values.add(String.valueOf((r + g + b) / 3));
      }
    }
    return String.join("\n", String.join(",", headers), String.join(",", values));
  }

  public void onChooseModel(ActionEvent ignored) {
    if (nonNull(modelFile = chooseModelFile())) {
      setModelLabelText(MODEL_BEING_LOADED);
      try {
        if (post(
            MODEL_METHOD, convertToJSON("path", replaceBackSlashes(modelFile))
        ).contains("ready")) {
          setModelLabelText(MODEL_READY);
        } else {
          setModelLabelText(MODEL_FAILED);
        }
        cleanCanvas();
        drawResult();
      } catch (IOException ex) {
        System.out.printf("error while loading model: %s%n", ex.getMessage());
      }
    }
  }

  public void onReset(ActionEvent ignored) {
    try {
      if (get(RESET_METHOD).contains("model was reset")) {
        setModelLabelText(MODEL_PLS_CHOOSE);
        draws = new LinkedList<>();
        cleanCanvas();
        drawResult();
      }
    } catch (IllegalStateException ex) {
      System.out.println("wtf");
    }
  }

  public void onInvertColors(ActionEvent ignored) throws IOException {
    background = background.equals(BLACK) ? WHITE : BLACK;
    brush = brush.equals(BLACK) ? WHITE : BLACK;
    cleanCanvas();
    draws.forEach(this::draw);
    evalCanvas();
  }

  public void onUndoDraw(ActionEvent ignored) throws IOException {
    try {
      cleanCanvas();
      draws.removeLast();
      if (draws.isEmpty()) {
        drawResult();
        return;
      }
      draws.forEach(this::draw);
      evalCanvas();
    } catch (NoSuchElementException e) {
      System.out.println("cannot reverse when nothings been done!");
    }
  }

  private Color getColor(Draw.Paint paint) {
    return paint.equals(Draw.Paint.BACKGROUND) ? background : brush;
  }

  public void cleanCanvas() {
    canvas.getGraphicsContext2D().setFill(background);
    canvas.getGraphicsContext2D().fillRect(0, 0, 400, 400);
    drawResult();
  }

  private void setModelLabelText(String s) {
    modelLabel.setText(format(s, nonNull(modelFile) ? modelFile.getName() : "null"));
  }

  public void setStage(Stage stage) {
    this.stage = stage;
  }
}
