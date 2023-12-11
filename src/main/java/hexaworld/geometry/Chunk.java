package hexaworld.geometry;

import lombok.Getter;

public class Chunk {
  @Getter
  Point position;
  public BlockType[] data = new BlockType[24];
  private enum BlockType{
    GRASS,
    WATER,
    STONE
  }
}
