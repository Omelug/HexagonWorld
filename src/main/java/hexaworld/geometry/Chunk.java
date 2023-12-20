package hexaworld.geometry;

import hexaworld.client.Client;
import hexaworld.client.Player;
import hexaworld.server.ServerConfig;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import lombok.Getter;

import java.io.Serializable;
import java.util.Random;

public class Chunk implements Serializable {
  @Getter
  private final Point position;
  private BlockType[] data;
  private Path hexagonPath;

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

  public void draw(Point vectorFromPlayer) {
    //TODO only follow player type
    //hexagonPath = Geometry.createHexagonPath(Player.getPosition().getX()+ vectorFromPlayer.getX(), Player.getPosition().getY()+ vectorFromPlayer.getY(), 2);
    //hexagonPath.setFill(Color.GREEN);

    //System.out.println("" +Player.getPosition() + vectorFromPlayer +"->" + (Player.getPosition().getX()+ vectorFromPlayer.getX())*Client.getVIEW_UNIT().getX());

    //Client.getRoot().getChildren().add(hexagonPath);
  }

  private enum BlockType{
    GRASS,
    WATER,
    ENERGY,
    STONE
  }
}
