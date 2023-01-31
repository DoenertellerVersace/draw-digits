package org.htw.drawdigits;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.Canvas;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

public class Utils {


  protected static String getGreyScaleCSVFromCanvas(Canvas canvas) {
    java.util.List<String> headers = new ArrayList<>();
    List<String> values = new ArrayList<>();
    BufferedImage buffered = Utils.getScaledDownImage(
        SwingFXUtils.fromFXImage(canvas.snapshot(null, null), null)
    );
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

  protected static BufferedImage getScaledDownImage(BufferedImage image) {
    return toBufferedImage(image.getScaledInstance(28, 28, BufferedImage.SCALE_AREA_AVERAGING));
  }

  protected static BufferedImage toBufferedImage(Image img) {
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
}
