package hexaworld.server;

import hexaworld.cli.CLog;
import hexaworld.server.commands.Command;
import lombok.Getter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server implements Runnable{
  static private final CLog log = new CLog(CLog.ConsoleColors.PURPLE);
  @Getter
  static private final List<ServerPlayer> players = new ArrayList<>();
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
    Thread TCPThread = new Thread(() -> {
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
    });
    TCPThread.start();
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
        String[] command = scanner.nextLine().toLowerCase().trim().split(" ");

        ServerPlayer player;
        switch (command[0]){
          case "pl", "playerlist" -> printPlayerList();
          case "chat" -> Chat.msgAll("[Server] " + command[1]);
          case "clear" -> clearConsole();
          case "kick","wl","eun","deny","accept" ->{
            player = getPlayerByName(command[1]);
            if (player == null){log.error(command[1] + " is not here");
              continue;
            }
            playerCommand(player,command);
          }
            default -> log.error("Invalid command " + command[0]);
          }
        }
    });
    startCLIThread.start();
  }

  private void playerCommand(ServerPlayer player, String[] command) {
    switch (command[0]){
      case "kick" -> {
        player.kick();
        Chat.msgAll(command[1] + " kicked out");
      }
      case "wl" ->{
        System.out.println(player.getName() + " waiting list: ");
        Iterator<Command> iterator = player.getWaitCmdList().iterator();
        while (iterator.hasNext()) {
          Command cmd = iterator.next();
          System.out.println(cmd);
        }
      }
      case "run","accept","deny" ->{
        if (player.getWaitCmdList().isEmpty()){
          log.error( player.getName()+ "'s waitCmdList is empty");
        }
        waitingListCommand(player,command[0]);
      }
      default -> throw new IllegalStateException("Unexpected value: " + command[0]);
    }
  }

  private void waitingListCommand(ServerPlayer player, String command) {
    switch (command){
      case "run" -> player.getWaitCmdList().get(0).run();
      case "deny" -> player.getWaitCmdList().get(0).deny(true);
      case "accept" -> player.getWaitCmdList().get(0).accept();
    }
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
    return result.orElse(null);
  }

  private void printPlayerList() {
    System.out.println("Player list:"); //TODO do some color table ?
    for (ServerPlayer player : players){
      System.out.println(player + "(" +player.getEnergy()+ ") pos:" + player.getPosition());
    }
  }

  private void startTick() {
    final long targetSleepTime = 1000 / ServerConfig.TPS;

    //log.debug("awdawdaw");
    Thread tickingThread = new Thread(() -> {

      while (true) {
        long startTime = System.currentTimeMillis();
        
        synchronized (players) {
          Iterator<ServerPlayer> iterator = players.iterator();
          while (iterator.hasNext()) {
            ServerPlayer player = iterator.next();
            synchronized(player) {
              // Perform operations on player
              player.tick();
              //Chat.msg(player, "TICK");
              player.addEnergy(1);
            }
          }
        }
        //TODO prehodit, aby to pocitalo, pak cekalo a pak odeslalo

        synchronized (players) {
          //log.debug("awdawd");
          Iterator<ServerPlayer> sendIter = players.iterator();
          while (sendIter.hasNext()) {
            ServerPlayer player = sendIter.next();
            //log.debug("Out " + player.getObjectOutputStream());
            synchronized(player) {
              player.sendTick();
            }
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
