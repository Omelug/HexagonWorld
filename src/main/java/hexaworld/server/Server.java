package hexaworld.server;

import hexaworld.cli.CLog;
import hexaworld.client.Player;
import hexaworld.geometry.Chunk;
import hexaworld.server.commands.Command;
import lombok.Getter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

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
    startTCP();
    startTick();
  }

  private void startCLI() {

    //TODO tohle musi byt hezci, enum nebo objekty na cliCmd
    Thread startCLIThread = new Thread(() -> {
      Scanner scanner = new Scanner(System.in);
      while (true) {
        String command = scanner.nextLine().trim();
        if (command.equalsIgnoreCase("pl") || command.equalsIgnoreCase("playerList")) {
          printPlayerList();
        }else if (command.startsWith("kick ")){
          ServerPlayer player = getPlayerByName(command.substring(5));
          if (player == null){log.error(command.substring(5) + " is not here");continue;}
          player.kick();
          Chat.msgAll(command.substring(5) + " kicked out");
        }else if( command.startsWith("chat ")){
          Chat.msgAll("[Server] " + command.substring(5));
        }else if( command.startsWith("wl ")){ //waitList
          ServerPlayer player = getPlayerByName(command.substring(3));
          if (player == null){log.error(command.substring(3) + " is not here");continue;}//TODO tohle je tu dvakrat a a to je hnusne
          System.out.println(player.getName() + " waiting list: ");

          Iterator<Command> iterator = player.getWaitCmdList().iterator();
          while (iterator.hasNext()) {
            Command cmd = iterator.next();
            System.out.println(cmd);
          }

        }else if(command.equals("clear")){
          clearConsole();
        }else if(command.startsWith("run ")){
          ServerPlayer player = getPlayerByName(command.substring(4));
          if (player == null){continue;}
          if (player.getWaitCmdList().isEmpty()){
            log.error( player.getName()+ "'s waitCmdList is empty");
            continue;
          }
          player.getWaitCmdList().get( player.getWaitCmdList().size()-1).run();
        }else if(command.startsWith("deny ")){
          ServerPlayer player = getPlayerByName(command.substring(5));
          if (player == null){continue;}
          if (player.getWaitCmdList().isEmpty()){
            log.error( player.getName()+ "'s waitCmdList is empty");
            continue;
          }
          player.getWaitCmdList().get( player.getWaitCmdList().size()-1).deny();
        }else if(command.startsWith("accept ")){
          ServerPlayer player = getPlayerByName(command.substring(7));
          if (player == null){continue;}
          if (player.getWaitCmdList().isEmpty()){
            log.error( player.getName()+ "'s waitCmdList is empty");
            continue;
          }
          player.getWaitCmdList().get( player.getWaitCmdList().size()-1).accept();
        } else{
          log.error("Invalid command " + command);
        }
      }
    });
    startCLIThread.start();
  }

  public static void clearConsole() { //TODO check if this work (dont work in IDE)
    try {
      final String os = System.getProperty("os.name");

      if (os.contains("Windows")) {
        new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
      } else {
        System.out.print("\033[H\033[2J");
        System.out.flush();
      }
    } catch (final Exception e) {
      log.error("No one can clean up a mess like this");
      //e.printStackTrace();
    }
  }

  public static ServerPlayer getPlayerByName(String name) {
    Optional<ServerPlayer> result = players.stream()
            .filter(player -> name.equals(player.getName()))
            .findFirst();
    if (result.isEmpty()){
      return null;
    }
    return result.get();
  }

  private void printPlayerList() {
    System.out.println("Player list:"); //TODO do some color table ?
    for (ServerPlayer player : players){
      System.out.println(player + "(" +player.getEnergy()+ ") pos:" + player.getPosition());
    }
  }

  private void startTick() {
    final int targetTPS = 20;
    final long targetSleepTime = 1000 / targetTPS;

    Thread tickingThread = new Thread(() -> {

      while (true) {
        long startTime = System.currentTimeMillis();

        Iterator<ServerPlayer> iterator = players.iterator();

        while (iterator.hasNext()) {
          ServerPlayer player = iterator.next();
          synchronized(player) {
            // Perform operations on player
            //FIXME player.tick();
            //Chat.msg(player, "TICK");
          }
        }
        Iterator<ServerPlayer> sendIter = players.iterator();

        while (sendIter.hasNext()) {
          ServerPlayer player = sendIter.next();
          synchronized(player) {
            player.sendTick();
          }
        }

        long elapsedTime = System.currentTimeMillis() - startTime;

        long sleepTime = Math.max(0, targetSleepTime - elapsedTime);

        try {
          Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    });
    tickingThread.start();
  }

}
