package hexaworld.net;

import hexaworld.CLog;
import hexaworld.server.ServerAPI;
import lombok.Getter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Packet {
  static final CLog log = new CLog(CLog.ConsoleColors.CYAN_BOLD);

  /*public static void send(ServerAPI.Socket socket, byte[] data){
    try {
      if (socket == null) {
        log.error("Socket is null");
        return;
      }

      OutputStream out = socket.getOutputStream();

      out.write(data);
      out.write("\n".getBytes());

    } catch (IOException e) {
      log.error("error sendDataTCP " + e.getMessage());
    }
  }*/

  public enum PacketType {
    LOGIN,
    COMMAND,
    CHAT,
    TICK,
    CHAT_UPDATE,
    CHUNK,
    CORRECTION;
  }

  @Getter
  PacketType ID;
  @Getter
  private byte[] data;

  public Packet(PacketType type, byte[] data) {

  }

}
