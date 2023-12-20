package hexaworld.client;

import hexaworld.geometry.Geometry;
import hexaworld.geometry.Point;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import hexaworld.server.ServerConfig;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

public class Player{

  @Getter @Setter
  private static Point position = new Point(0,0);
  private final Color hexagonColor = Color.CYAN;//TODO const
  private Path hexagonPath;
  private double size = 1;
  @Getter
  private String name = "Player42";
  @Getter @Setter
  private int energy = 0;

  public Player(Pane root, String name) {
    hexagonPath = Geometry.createHexagonPath(position.getX(), position.getY(), size);
    hexagonPath.setFill(hexagonColor);
    root.getChildren().add(hexagonPath);
    this.name = name;
  }

  void move(double deltaX, double deltaY) {
    position.add(deltaX,deltaY);

    hexagonPath.setTranslateX(position.getX());
    hexagonPath.setTranslateY(position.getY());
  }

  public void handleKeyPress(KeyCode code) {
    //Client.log.debug("Pressed " + code);
    switch (code) {
      case W:
        move(0, -size);
        break;
      case S:
        move(0, size);
        break;
      case A:
        move(-Geometry.getTriangleV(size), 0);
        move(0, size/2);
        break;
      case Q:
        move(-Geometry.getTriangleV(size), 0);
        move(0, -size/2);
        break;
      case D:
        move(Geometry.getTriangleV(size), 0);
        move(0, size/2);
        break;
      case E:
        move(Geometry.getTriangleV(size), 0);
        move(0, -size/2);
        break;
    }
  }
  private static final double MIN_SCALE = 0.1;
  public void zoom(double zoomFactor) {
    double currentScale = Math.max(zoomFactor * hexagonPath.getScaleX(), MIN_SCALE);
    hexagonPath.setScaleX(currentScale);
    hexagonPath.setScaleY(currentScale);
    size *= zoomFactor;
  }
  @Override
  public String toString() {
    return String.format("%s[%f,%f], energy: %d", name, position.getX(), position.getY(), energy);
  }
}
