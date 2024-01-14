package hexaworld.server;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import java.util.HashMap;
import java.util.Map;

public class ServerConfig {
  @Getter
  private static Map<ServerAPI.COMMAND, CmdInfo> commandTable = new HashMap<>();

  @AllArgsConstructor
  @Data
  public static class CmdInfo {
    @Getter
    private int energy;
    @Getter
    private int time;
    @Getter
    private int receiveObjCnt;
  }

  public static int
          FREE_CHUNK = 19,
          PLAYER_WAIT_LIST_SIZE = 10,
          TCP_PORT = 42,
          TPS = 10;
  public static final double MAP_SEED = 42;

  public static void loadEnergyTable(){ //FIXME load from json
    commandTable.put(ServerAPI.COMMAND.LOAD_CHUNK,new CmdInfo(1,1,1));
    commandTable.put(ServerAPI.COMMAND.MOVE,new CmdInfo(1,0,1));
    commandTable.put(ServerAPI.COMMAND.CHANGE_NAME,new CmdInfo(1,0,1));
  }
}
