package hexaworld.server;

import hexaworld.geometry.Chunk;
import hexaworld.geometry.Point;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Map{
  Set<Chunk> loadedChunks = new HashSet<>();
  static Chunk loadChunk(Point point) {
      Chunk chunk = Chunk.generateChunk(point);
      //chunk.loadChanges(); //TODO load changes
      return chunk;
  }
}
