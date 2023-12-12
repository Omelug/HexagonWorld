package hexaworld.geometry;

import hexaworld.server.ServerConfig;
import lombok.Getter;

import java.util.Random;

public class Chunk {
  @Getter
  private final Point position;
  private BlockType[] data;

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
    new Chunk(position, newData);
  }

  private enum BlockType{
    GRASS,
    WATER,
    ENERGY,
    STONE
  }
}
