package hexaworld.server;

import hexaworld.CLog;
import hexaworld.net.Packet;
import lombok.Getter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class Server implements Runnable{
  static private final CLog log = new CLog(CLog.ConsoleColors.PURPLE);
  @Getter
  static private List<ServerPlayer> players = new ArrayList<>();
  @Getter
  static public final Chat chat = new Chat();

  public static void main(String[] args) {

   /* Options options = new Options();
    CommandLineParser parser = new DefaultParser();

    options.addOption("TCPport", true, "TCP listening port");
    options.addOption("ET", true, "EnergyTableFile");**/
    ServerConfig.loadEnergyTable(); //TODO change by options
    Server server = new Server();
    server.run();

    log.info("STOP");
  }

  private static void startTCP() {
    try {
      ServerSocket socketTCP = new ServerSocket(ServerConfig.TCP_PORT);
      Thread tcpStart = new Thread(() -> {
        while (true) {
          try {
            Socket clientSocket = socketTCP.accept();
            log.info("Client connected [" + clientSocket.getInetAddress() + ":" + clientSocket.getPort()+"]");
            players.add(new ServerPlayer(clientSocket));
          } catch (IOException e) {
            log.error(e.getMessage());
            e.printStackTrace();
          }
        }
      });
      tcpStart.start();
      try {
        tcpStart.join();
      } catch (InterruptedException e) {
        log.error("Thread interrupted while waiting for tcpStart to finish: " + e.getMessage());
      }
    } catch (IOException e) {
      log.error("Listening TCP port was not opened " + e.getMessage());
    }
  }

  public static String getPlayersNames() {
    StringJoiner joiner = new StringJoiner(", ", "[", "]");
    for (ServerPlayer p : players) {
      joiner.add(p.getName());
    }
    return joiner.toString();
  }

  @Override
  public void run() {
    startTick();
    startTCP();
  }

  private void startTick() {
  }

  private class Map{
    void loadChunk() {

    }
  }

}
