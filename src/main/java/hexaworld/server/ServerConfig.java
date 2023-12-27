package hexaworld.server;

import lombok.Getter;
import java.util.HashMap;
import java.util.Map;

public class ServerConfig {
  @Getter
  private static Map<ServerAPI.COMMAND, Integer> energyTable = new HashMap<>();

  public static int
          FREE_CHUNK = 19,
          PLAYER_STACK = 10,
          TCP_PORT = 42,
          TPS = 10;
  public static final double MAP_SEED = 42;

  public static void loadEnergyTable(){ //FIXME load from json
    energyTable.put(ServerAPI.COMMAND.LOAD_CHUNK,3);
    energyTable.put(ServerAPI.COMMAND.MOVE,1);
    energyTable.put(ServerAPI.COMMAND.CHANGE_NAME,1);
  }
}
