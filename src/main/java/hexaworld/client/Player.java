package hexaworld.client;

import hexaworld.geometry.Geometry;
import hexaworld.geometry.Point;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import hexaworld.server.ServerConfig;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

import static javafx.scene.input.KeyCode.W;

public class Player{

  @Getter @Setter
  private static Point position = new Point(0,0);
  private static final Color hexagonColor = Color.CYAN;//TODO const
  @Getter
  private static Path hexagonPath;
  private static double size = 0.8;
  @Getter
  private String name = "Player42";
  @Getter @Setter
  private int energy = 0;
  private static final double MIN_SCALE = 0.1;

  public Player( String name) {
    this.name = name;
  }
  public static void draw(GraphicsContext gc){
    /*hexagonPath = Geometry.createHexagonPath(position.getX(), position.getY(), size);
    hexagonPath.setFill(hexagonColor);
    if (!Client.getRoot().getChildren().contains(hexagonPath)) {
      Client.getRoot().getChildren().add(hexagonPath);
    }*/
    gc.setFill(Color.CYAN);
    Geometry.drawHexagon(gc,position.getX(), position.getY(), size);
  }

  void move(double deltaX, double deltaY) {
    position.add(deltaX,deltaY);
  }

  public void handleKeyPress(KeyCode code) {
    //Client.log.debug("Pressed " + code);
    //TODO sent to server and check correction if is not possible

    Map<KeyCode, Geometry.HEXA_MOVE> keyBindMove = Map.of(
            KeyCode.W, Geometry.HEXA_MOVE.UP,
            KeyCode.S, Geometry.HEXA_MOVE.DOWN,
            KeyCode.A, Geometry.HEXA_MOVE.LEFT_DOWN,
            KeyCode.Q, Geometry.HEXA_MOVE.LEFT_UP,
            KeyCode.D, Geometry.HEXA_MOVE.RIGHT_DOWN,
            KeyCode.E, Geometry.HEXA_MOVE.RIGHT_UP
    );

    if(null != keyBindMove.get(code)){
      position.add(keyBindMove.get(code));
      //Geometry.moveNoPlayerR(keyBindMove.get(code)); //TODO čelem vzad, pozadi se pohybuje opacne a jsou to nějak male kroky
      ClientAPI.playerCanvasUpdate();
      return;
    }
    Map<KeyCode, Point> keyBindShift = Map.of(
            KeyCode.UP, new Point(0,Client.getZOOM()),
            KeyCode.DOWN, new Point(0,-Client.getZOOM()),
            KeyCode.LEFT, new Point(Client.getZOOM(),0),
            KeyCode.RIGHT, new Point(-Client.getZOOM(),0)
    );

    Point deltaShift = keyBindShift.get(code);
    if(null != deltaShift){
      Client.getShift().add(deltaShift);
      ClientAPI.canvasUpdate();
    }

  }

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
