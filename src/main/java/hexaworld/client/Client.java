package hexaworld.client;

import hexaworld.cli.CLog;
import hexaworld.cli.CliCommand;
import hexaworld.geometry.Chunk;
import hexaworld.geometry.Geometry;
import hexaworld.geometry.Point;
import hexaworld.net.Change;
import hexaworld.net.Packet;
import hexaworld.net.TCPReceiver;
import hexaworld.server.ServerAPI;
import hexaworld.server.commands.Command;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.cli.*;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;

public class Client extends Application implements TCPReceiver {
    public static final CLog log = new CLog(CLog.ConsoleColors.GREEN);


    public enum ViewType{FOLLOW}


  @Getter @Setter
    private static Point shift = new Point(0,0);
    @Getter @Setter
    private static ViewType viewType = ViewType.FOLLOW; //TODO constant to config
    @Getter
    private static Socket tcpSocket;
    @Getter
    @FXML
    private static final Pane root = new Pane();
    @Getter @Setter
    private static Canvas mapCanvas, playerCanvas;

    @Getter @Setter
    private static Player player;

    @Getter @Setter
    private static Grid grid;

    @Getter
    private static final double ZOOM = 30; //start_zoom

    @Getter
    public static Point VIEW_UNIT = new Point(Math.sqrt(3)/2*ZOOM,0.5*ZOOM); //new Point((Math.sqrt(3)/4)*ZOOM,0.5*ZOOM);

    @Getter @Setter
    private static List<Chunk> chunks = new ArrayList<>();

    private static ScriptEngine engine;

    private static final ClientAPI clientAPI  = new ClientAPI();

    //@Getter @Setter
    //private static Point shift;

    @Override
    public void start(Stage primaryStage) {
        Platform.setImplicitExit(false); //DONT TOUCH, solve FX error ??? spíš ne

        //System.out.println("test " + Math.sqrt(3)/2*ZOOM );
        //root.setStyle("-fx-background-color: #777474;");
        //Grid grid = new Grid(Grid.GridType.TRIANGLE,canvas);

        Scene scene = new Scene(root, 500, 500);

        scene.setOnScroll(scrollEvent -> {
            double zoomFactor = Math.exp(scrollEvent.getDeltaY() / 100.0);
            VIEW_UNIT = new Point(VIEW_UNIT.getX()*zoomFactor,VIEW_UNIT.getY()*zoomFactor);
            ClientAPI.canvasUpdate();
            scrollEvent.consume();
        });

        scene.setOnKeyPressed(keyEvent -> player.handleKeyPress(keyEvent.getCode()));
        scene.widthProperty().addListener((observable, oldValue, newValue) -> ClientAPI.canvasUpdate());
        scene.heightProperty().addListener((observable, oldValue, newValue) -> ClientAPI.canvasUpdate());
        
        primaryStage.setTitle("Hexagon World");
        primaryStage.setScene(scene);
        ClientAPI.canvasUpdate();

        primaryStage.show();

    }

    private void login(InetAddress serverIP,int tcpPort, String username){

        ScriptEngineManager manager = new ScriptEngineManager();
        engine = manager.getEngineByName("python");

        player = new Player(username);
        //init TCP connection
        try {
            tcpSocket =  new Socket(serverIP, tcpPort);
        } catch (IOException e) {
            log.terror("Socket creation of [" + serverIP +":"+tcpPort +"] issue:" + e.getMessage());
        }
        ClientAPI.init();

        Thread tcpStart = new Thread(() -> receiveTCP(tcpSocket));
        tcpStart.start();

        ClientAPI.changeName(username);
        ClientAPI.login();
        ClientAPI.loadAround();
    }

    public static void main(String[] args) {

        Options options = new Options();
        CommandLineParser parser = new DefaultParser();

        options.addOption("ip", true, "server IP");
        options.addOption("TCPport", true, "TCP port");
        options.addOption("name", true, "Player name");

        try {
            CommandLine cmd = parser.parse(options, args);
            InetAddress serverIP = InetAddress.getByName(cmd.getOptionValue("ip"));
            int tcpPort = Integer.parseInt(cmd.getOptionValue("TCPport"));

            String name = cmd.getOptionValue("name");

            log.info("Client started");
            log.info("Server: " + serverIP + ":" + tcpPort);
            log.info("Player name: " + name);

            Client client = new Client();
            client.login(serverIP,tcpPort, name);
            startCLI();
            launch();

        } catch (UnknownHostException e) {
            log.error("Unknown host");
        } catch (ParseException e) {
            log.error("Argument parsing error "+ e.getMessage());
        }
    }

    public static final Set<CliCommand> cmdList = Set.of(
      new CliCommand("help", "show help list"),
      new CliCommand("player","show client player data"),
      new CliCommand("chat", "chat <msg> for send msg"),
      new CliCommand("api", "api <cmd> for running ClientAPI.<cmd> form python ")
    );

    private static void startCLI() {
        String script = "ClientAPI.changeName(name)";
        try {
            engine.put("ClientAPI", clientAPI);
            engine.put("name", player.getName());

            engine.eval(script);
        } catch (ScriptException e) {
            log.error("Script "+script +" exeption");
        }

        Thread startCLIThread = new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (true) {

              String command = scanner.nextLine().trim();

              if (command.equalsIgnoreCase("help")) {
                  for (CliCommand cmd : cmdList) {
                    ClientChat.clientChat(CLog.ConsoleColors.CYAN+ cmd.getCmd()+" : "+CLog.ConsoleColors.WHITE +cmd.getDescription());
                  }
                }else if (command.startsWith("player")){
                    ClientChat.clientChat("Player: "+Client.getPlayer());
                }else if (command.startsWith("chat ")){
                    ClientAPI.chat(command.substring(4));
                }else if (command.startsWith("api ")){
                    String apiCmd = "ClientAPI."+ command.substring(4);
                    try {
                        engine.eval(apiCmd);
                    } catch (ScriptException e) {
                        log.error("Error executing " +  apiCmd + e.getMessage());
                    }
                }else{
                    log.error("Invalid command " + command);
                    log.info("help or h for help list");
                }
            }
        });
        startCLIThread.start();
    }

    @Override
    public void receiveTCP(Socket tcpClientSocket) {
        InputStream inputStream;
        ObjectInputStream objectInputStream;

        try {
            inputStream = tcpClientSocket.getInputStream();
            objectInputStream = new ObjectInputStream(inputStream);
        } catch (IOException e){
            log.terror("Error client during stream creation " + e.getMessage() );
            return;
        }

        while(true){
            try {
                Packet.PacketType packetType = (Packet.PacketType) objectInputStream.readObject();

                switch (packetType){
                  case CHAT -> {
                    ClientChat.clientChat( (String) objectInputStream.readObject());
                  }
                  case LOGIN -> {
                    Player.setPosition((Point) objectInputStream.readObject());
                    player.setEnergy(objectInputStream.readInt());
                  }
                  case CHUNK -> {
                    Chunk newChunk = (Chunk) objectInputStream.readObject();

                    boolean chunkExists = false;
                    for (int i = 0; i < chunks.size(); i++) {
                      Chunk existingChunk = chunks.get(i);
                      if (Point.same(existingChunk.getPosition(), newChunk.getPosition())) {
                        chunkExists = true;
                        chunks.set(i, newChunk);
                      }
                    }
                    if (!chunkExists) {
                      chunks.add(newChunk);
                      if(mapCanvas != null){
                        newChunk.draw();
                      }
                    }
                  }
                  case CORRECTION -> {
                    ServerAPI.COMMAND command = (ServerAPI.COMMAND) objectInputStream.readObject();
                    if (command == ServerAPI.COMMAND.MOVE) {

                      Geometry.HEXA_MOVE move = (Geometry.HEXA_MOVE) objectInputStream.readObject(); //move what was not accepted by server

                      /*double newPosX = objectInputStream.readDouble();
                      double newPosY = objectInputStream.readDouble();
                      Point newPos = new Point(newPosX,newPosY);*/

                      ClientChat.clientChat("undo " + move);

                      /**Platform.runLater(() -> {
                       ClientAPI.movePlayerFollow(Geometry.rotate180(move));
                       });*/
                      //log.debug("" + Player.getPosition());
                    }
                  }
                  case TICK -> {
                    Change.CHANGE change = (Change.CHANGE) objectInputStream.readObject();
                    log.debug("TICK " + change);
                      while (change != Change.CHANGE.STOP){
                       switch (change){
                         case POSITION -> {
                         Point pos = (Point) objectInputStream.readObject();
                         log.debug("TICK position " + pos);
                         }
                         case ENERGY -> {
                         player.setEnergy(objectInputStream.readInt());
                         }
                       }
                        change = (Change.CHANGE) objectInputStream.readObject();
                     }
                  }
                }
            } catch(EOFException e) {
                continue;
            }catch (IOException | ClassNotFoundException e) {
                log.terror("Connection lost");
                break;
            }
        }
    }

    private static class ClientChat{
        public static void clientChat(String msg){
            System.out.print("chat: " +msg+ "\n" + CLog.ConsoleColors.RESET);
        }
    }

}