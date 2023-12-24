package hexaworld.geometry;

import hexaworld.client.Client;
import hexaworld.client.Player;
import hexaworld.server.ServerConfig;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;
import java.util.Random;

public class Chunk implements Serializable {
  @Getter
  private final Point position;
  private BlockType[] data;
  private Path hexagonPath;
  public final short CHUNK_SIZE = 2;

  public Chunk(Point position, BlockType[] data) {
    this.position = position;
    this.data = data;
  }

  public static Chunk generateChunk(Point position) {
    BlockType[] newData = new BlockType[24];
    Random random = new Random((long) ServerConfig.MAP_SEED);

    BlockType[] values = BlockType.values();
    for (int i = 0; i < newData.length; i++) {
      newData[i] = values[random.nextInt(values.length)];
    }
    return new Chunk(position, newData);
  }

  public void draw() {

    //TODO only follow player type
    //hexagonPath = Geometry.createHexagonPath(Player.getPosition().getX()+ vectorFromPlayer.getX(), Player.getPosition().getY()+ vectorFromPlayer.getY(), 2);
    //System.out.println("" +Player.getPosition() + vectorFromPlayer +"->" + (Player.getPosition().getX()+ vectorFromPlayer.getX())*Client.getVIEW_UNIT().getX());
    hexagonPath = Geometry.createHexagonPath(position.getX(), position.getY(), CHUNK_SIZE);
    hexagonPath.setFill(Color.GREEN);

    Client.getRoot().getChildren().add(hexagonPath);
  }

  /*private void drawTrianglesInHexagon(double x, double y, double size) {

    gc = Client.get.getGraphicsContext2D();

    gc.setFill(Color.RED); // Change the color of the triangles as needed
    gc.setStroke(Color.BLACK);

    double triangleHeight = size * Math.sqrt(3) / 2;

    double[] xPoints = new double[3];
    double[] yPoints = new double[3];

    // Draw triangles within hexagon
    for (int i = 0; i < 6; i++) {
      double angle1 = 2.0 * Math.PI * i / 6;
      double angle2 = 2.0 * Math.PI * (i + 1) / 6;

      xPoints[0] = x;
      yPoints[0] = y;

      xPoints[1] = x + size * Math.cos(angle1);
      yPoints[1] = y + size * Math.sin(angle1);

      xPoints[2] = x + size * Math.cos(angle2);
      yPoints[2] = y + size * Math.sin(angle2);

      // Draw each triangle
      gc.fillPolygon(xPoints, yPoints, 3);
      gc.strokePolygon(xPoints, yPoints, 3);
    }
  }*/


  @AllArgsConstructor
  private enum BlockType {
    GRASS(Color.GREEN),
    WATER(Color.BLUE),
    ENERGY(Color.YELLOW),
    STONE(Color.GRAY);

    @Getter
    private final Color color;

  }
}
