package org.htw.drawdigits;

import javafx.util.Pair;

import java.util.List;

public class Draw {
  private final List<Pair<Double, Double>> points;
  private final Double size;
  private final Paint paint;

  public Draw(List<Pair<Double, Double>> points, Double size, Paint paint) {
    this.points = points;
    this.size = size;
    this.paint = paint;
  }

  public List<Pair<Double, Double>> getPoints() {
    return points;
  }

  public Double getSize() {
    return size;
  }

  public Paint getPaint() {
    return paint;
  }

  public enum Paint {
    BACKGROUND, BRUSH
  }
}
