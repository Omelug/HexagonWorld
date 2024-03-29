package hexaworld.server;

import hexaworld.client.Client;
import hexaworld.net.Packet;
import java.io.IOException;

public class Chat {

  public static void msgAll(String msg){
    System.out.println(msg +"->" + Server.getPlayersNames());
    for (ServerPlayer p : Server.getPlayers()){
      msg(p, msg);
    }
  }
  public static void msg(ServerPlayer player, String msg){
    try {
      player.getObjectOutputStream().writeObject(Packet.PacketType.CHAT);
      player.getObjectOutputStream().writeObject(msg);
      player.getObjectOutputStream().flush();
    } catch (IOException e) {
      Client.log.error("Error Chat connection msg: \" " + msg+"\"");
    }
  }
}
