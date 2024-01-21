package hexaworld.client;

import hexaworld.geometry.Geometry;
import hexaworld.geometry.Point;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.Setter;
import java.util.Map;

public class Player{

  @Getter @Setter
  private static Point position = new Point(0,0);
  //TODO move client const to config file
  private static final Color hexagonColor = Color.CYAN;

  private final Map<KeyCode, Geometry.HEXA_MOVE> keyBindMove = Map.of(
          KeyCode.W, Geometry.HEXA_MOVE.UP,
          KeyCode.S, Geometry.HEXA_MOVE.DOWN,
          KeyCode.A, Geometry.HEXA_MOVE.LEFT_DOWN,
          KeyCode.Q, Geometry.HEXA_MOVE.LEFT_UP,
          KeyCode.D, Geometry.HEXA_MOVE.RIGHT_DOWN,
          KeyCode.E, Geometry.HEXA_MOVE.RIGHT_UP
  );
  Map<KeyCode, Point> keyBindShift = Map.of(
          KeyCode.UP, new Point(0,Client.getZOOM()),
          KeyCode.DOWN, new Point(0,-Client.getZOOM()),
          KeyCode.LEFT, new Point(Client.getZOOM(),0),
          KeyCode.RIGHT, new Point(-Client.getZOOM(),0)
  );

  private static final double size = 0.8;

  @Getter
  private final String name;
  @Getter @Setter
  private int energy = 0;

  public Player( String name) {
    this.name = name;
  }
  public static void draw(GraphicsContext gc){
    gc.setFill(hexagonColor);
    Geometry.drawHexagon(gc,position.getX(), position.getY(), size);
  }

  public static void moveFollow(Geometry.HEXA_MOVE move) {
    if(Client.getViewType() == Client.ViewType.FOLLOW){
      Geometry.HEXA_MOVE move180 = Geometry.rotate180(move);
      Client.getShift().add(new Point(move180.getX()*Client.getVIEW_UNIT().getX(),move180.getY()*Client.getVIEW_UNIT().getY()));
      position.add(move);
      ClientAPI.canvasUpdate();
    }
  }
  public static void moveFollow(double moveX, double moveY) {
    if(Client.getViewType() == Client.ViewType.FOLLOW){
      Client.getShift().add(new Point(-moveX*Client.getVIEW_UNIT().getX(),-moveY*Client.getVIEW_UNIT().getY()));
      position.add(moveX, moveY);
      ClientAPI.canvasUpdate();
    }
  }

  void move(double deltaX, double deltaY) {
    position.add(deltaX,deltaY);
  }

  // Variables to control the rate
  private static final long MOVE_INTERVAL = 100; // Interval in milliseconds
  private long lastMoveTime = 0;
  public void handleKeyPress(KeyCode code) {
    //TODO sent to server and check correction if is not possible

    Geometry.HEXA_MOVE move = keyBindMove.get(code);
    if (move == null){
      return;
    }

    long currentTime = System.currentTimeMillis();

    if (currentTime - lastMoveTime >= MOVE_INTERVAL) {
      ClientAPI.move(move); //send move to server
      lastMoveTime = currentTime;
    }

   /* Point deltaShift = keyBindShift.get(code);
    if(null != deltaShift){
      Client.getShift().add(deltaShift);
      ClientAPI.canvasUpdate();
    }*/

  }

  @Override
  public String toString() {
    return String.format("%s[%f,%f], energy: %d", name, position.getX(), position.getY(), energy);
  }
}
