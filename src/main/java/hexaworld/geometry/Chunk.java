package hexaworld.geometry;

import hexaworld.client.Client;
import hexaworld.server.ServerConfig;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;
import java.util.*;

public class Chunk implements Serializable {
  @Getter
  private final Point position;
  private BlockType[] data;
  public static final short CHUNK_SIZE = 2;

  //TODO resourcePack

  static Map<BlockType,Color> rp = Map.of(
          BlockType.GRASS, Color.GREEN,
          BlockType.STONE, Color.GRAY,
          BlockType.ENERGY, Color.LIGHTGREEN,
          BlockType.WATER, Color.BLUE
  );


  public Chunk(Point position, BlockType[] data) {
    this.position = position;
    this.data = data;
  }

  public static Chunk generateChunk(Point position) {
    BlockType[] newData = new BlockType[24];
    Random random = new Random((long) (ServerConfig.MAP_SEED + position.getY() + position.getX()));

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
    gc.setStroke(Color.BLACK);

    List<Chunk> chunks = Client.getChunks();
    Iterator<Chunk> iterator = chunks.iterator();
    while (iterator.hasNext()) {
      Chunk chunk = iterator.next();

      for (int i = 0; i < Geometry.HEXA_MOVE.values().length; i++ ) { //TODO all triangles

        Geometry.HEXA_MOVE first_point =  Geometry.HEXA_MOVE.get(i);
        Geometry.HEXA_MOVE second_point =  Geometry.HEXA_MOVE.get(i+1);

        double[] xPoints = {chunk.getPosition().getX(), chunk.getPosition().getX() + first_point.getX(), chunk.getPosition().getX() + second_point.getX()};
        double[] yPoints = {chunk.getPosition().getY(), chunk.getPosition().getY() + first_point.getY(), chunk.getPosition().getY() + second_point.getY()};

        Geometry.multiplyArray(xPoints, Client.getVIEW_UNIT().getX());
        Geometry.multiplyArray(yPoints, Client.getVIEW_UNIT().getY());

        Geometry.addArray(xPoints, Client.getRoot().getWidth()/2 + Client.getShift().getX());
        Geometry.addArray(yPoints, Client.getRoot().getHeight()/2 + Client.getShift().getY());

        gc.setFill(rp.get(chunk.data[4*i+3]));
        gc.fillPolygon(xPoints, yPoints, 3);
        gc.strokePolygon(xPoints, yPoints, 3);

        xPoints[0] += Geometry.HEXAGON_BORDERS.get(i).getX()*Client.getVIEW_UNIT().getX();
        yPoints[0] += Geometry.HEXAGON_BORDERS.get(i).getY()*Client.getVIEW_UNIT().getY();

        gc.setFill(rp.get(chunk.data[4*i]));
        gc.fillPolygon(xPoints, yPoints, 3);
        gc.strokePolygon(xPoints, yPoints, 3);

        xPoints[1] += Geometry.HEXAGON_BORDERS.get(i+1).getX()*Client.getVIEW_UNIT().getX();
        yPoints[1] += Geometry.HEXAGON_BORDERS.get(i+1).getY()*Client.getVIEW_UNIT().getY();

        gc.setFill(rp.get(chunk.data[4*i+1]));
        gc.fillPolygon(xPoints, yPoints, 3);
        gc.strokePolygon(xPoints, yPoints, 3);

        xPoints[1] -= Geometry.HEXAGON_BORDERS.get(i+1).getX()*Client.getVIEW_UNIT().getX();
        yPoints[1] -= Geometry.HEXAGON_BORDERS.get(i+1).getY()*Client.getVIEW_UNIT().getY();

        xPoints[2] += Geometry.HEXAGON_BORDERS.get(i-1).getX()*Client.getVIEW_UNIT().getX();
        yPoints[2] += Geometry.HEXAGON_BORDERS.get(i-1).getY()*Client.getVIEW_UNIT().getY();

        gc.setFill(rp.get(chunk.data[4*i+2]));
        gc.fillPolygon(xPoints, yPoints, 3);
        gc.strokePolygon(xPoints, yPoints, 3);

      }
    }
  }


  @AllArgsConstructor
  private enum BlockType {
    GRASS(Color.GREEN),
    WATER(Color.BLUE),
    ENERGY(Color.YELLOW),
    STONE(Color.GRAY);

    private final Color color;

  }
}
