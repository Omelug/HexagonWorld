package hexaworld.geometry;

import hexaworld.client.Client;
import hexaworld.server.ServerConfig;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;
import java.util.*;

import static hexaworld.client.Client.VIEW_UNIT;

public class Chunk implements Serializable {
  @Getter
  private final Point position;
  private BlockType[] data;
  private Path hexagonPath = null;
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
    /*hexagonPath = Geometry.createHexagonPath(position.getX(), position.getY(), CHUNK_SIZE);
    hexagonPath.setFill(Color.GREEN);

    if (!Client.getRoot().getChildren().contains(hexagonPath)) {
      Client.getRoot().getChildren().add(hexagonPath);
    }*/
    GraphicsContext gc = Client.getMapCanvas().getGraphicsContext2D();
    Geometry.drawHexagon(gc,position.getX(), position.getY(), CHUNK_SIZE);
  }

  public static void drawTriangles(GraphicsContext gc) {

    //System.out.println(" " + Client.getCanvas().getWidth() + " " + Client.getCanvas().getHeight());

    gc.setStroke(Color.BLACK);
    gc.setFill(Color.RED);

    List<Chunk> chunks = Client.getChunks();
    Iterator<Chunk> iterator = chunks.iterator();
    while (iterator.hasNext()) {
      Chunk chunk = iterator.next();

      double[] xPoints = {chunk.getPosition().getX(), chunk.getPosition().getX() + Geometry.HEXA_MOVE.UP.getX(), chunk.getPosition().getX() + Geometry.HEXA_MOVE.RIGHT_UP.getX()};
      double[] yPoints = {chunk.getPosition().getY(), chunk.getPosition().getY() + Geometry.HEXA_MOVE.UP.getY(), chunk.getPosition().getY() + Geometry.HEXA_MOVE.RIGHT_UP.getY()};
      //TODO all triangles

      Geometry.multiplyArray(xPoints, Client.getVIEW_UNIT().getX());
      Geometry.multiplyArray(yPoints, Client.getVIEW_UNIT().getY());

      Geometry.addArray(xPoints, Client.getRoot().getWidth()/2);
      Geometry.addArray(yPoints, Client.getRoot().getHeight()/2);

      Geometry.addArray(xPoints, Client.getShift().getX());
      Geometry.addArray(yPoints, Client.getShift().getY());

      //System.out.println("xPoints: " + Arrays.toString(xPoints));
      //System.out.println("yPoints: " + Arrays.toString(yPoints));

      gc.fillPolygon(xPoints, yPoints, 3);
      gc.strokePolygon(xPoints, yPoints, 3);
    }
  }


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
