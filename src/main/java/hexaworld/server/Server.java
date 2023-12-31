package hexaworld.server;

import hexaworld.cli.CLog;
import lombok.Getter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
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
            log.terror(e.getMessage());
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
    startCLI();
    startTick();
    startTCP();
  }

  private void startCLI() {

    Thread startCLIThread = new Thread(() -> {
      Scanner scanner = new Scanner(System.in);
      while (true) {
        String command = scanner.nextLine().trim();
        if (command.equalsIgnoreCase("pl") || command.equalsIgnoreCase("playerList")) {
          printPlayerList();
        }else if (command.startsWith("kick ")){
          ServerPlayer player = getPlayerByName(command.substring(5));
          if (player == null){
            log.error(command.substring(5) + "t is not here");
            continue;
          }
          player.kick();
          Chat.msgAll(command.substring(5) + " kicked out");
        }else if( command.startsWith("chat ")){
          Chat.msgAll("[Server] " + command.substring(5));
        }else{
          log.error("Invalid command " + command);
        }
      }
    });
    startCLIThread.start();
  }
  //only first player of this name
  private ServerPlayer getPlayerByName(String name) {
    for (ServerPlayer player : players){
      if (name.equals(player.getName())){
        return player;
      }
    }
    return null;
  }

  private void printPlayerList() {
    log.info("Player list:");
    for (ServerPlayer player : players){
      log.info(player.getName() + "(" +player.getEnergy()+ ") pos:" + player.getPosition());
    }
  }

  private void startTick() {
  }

}
