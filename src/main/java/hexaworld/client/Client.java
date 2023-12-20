package hexaworld.client;

import hexaworld.CLog;
import hexaworld.geometry.Chunk;
import hexaworld.geometry.Geometry;
import hexaworld.geometry.Point;
import hexaworld.net.Packet;
import hexaworld.net.TCPReceiver;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Client extends Application implements TCPReceiver {
    public static final CLog log = new CLog(CLog.ConsoleColors.GREEN);

    @Getter
    private static Socket tcpSocket;
    @Getter
    private static final Pane root = new Pane();

    @Getter @Setter
    private static Player player;

    @Getter @Setter
    private static Grid grid;

    @Getter @Setter
    private static double ZOOM = 15;
    @Getter
    private static final Point VIEW_UNIT = new Point(Math.sqrt(3)/2*ZOOM,0.5*ZOOM); //new Point((Math.sqrt(3)/4)*ZOOM,0.5*ZOOM);

    @Getter @Setter
    private static List<Chunk> chunks = new ArrayList<>();

    private static ScriptEngine engine;

    private static final ClientAPI clientAPI  = new ClientAPI();
    //@Getter
    // static final Canvas canvas = new Canvas(320, 400);
    @Override
    public void start(Stage primaryStage) {
        System.out.println("test " + Math.sqrt(3)/2*ZOOM );
        //root.setStyle("-fx-background-color: #777474;");
        //Grid grid = new Grid(Grid.GridType.TRIANGLE,canvas);
        //root.getChildren().add(canvas);

        Scene scene = new Scene(root, 500, 500);
        /*scene.setOnScroll(scrollEvent -> {
            //Client.log.debug("scrollEvent");
            double zoomFactor = Math.exp(scrollEvent.getDeltaY() / 100.0);
            player.zoom(zoomFactor);
            scrollEvent.consume();
        });*/
        scene.setOnKeyPressed(keyEvent -> player.handleKeyPress(keyEvent.getCode()));
        primaryStage.setTitle("Hexagon World");
        primaryStage.setScene(scene);

        /*Path path1 = Geometry.createHexagonPath(7, 7, 2);
        path1.setFill(Color.GREEN);
        Client.getRoot().getChildren().add(path1);

        Path path2 = Geometry.createHexagonPath(3, 7 ,2);
        path2.setFill(Color.RED);
        Client.getRoot().getChildren().add(path2);

        Path path3 = Geometry.createHexagonPath(3, 7 ,1);
        path3.setFill(Color.CYAN);
        Client.getRoot().getChildren().add(path3);*/

        primaryStage.show();

    }

    private void login(InetAddress serverIP,int tcpPort, String username){

        ScriptEngineManager manager = new ScriptEngineManager();
        engine = manager.getEngineByName("python");

        player = new Player(root,username);
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
        } catch (org.apache.commons.cli.ParseException e) {
            log.error("Argument parsing error "+ e.getMessage());
        }
    }
    private void tick(){

    }
    private static void startCLI() {
        String script = "ClientAPI.changeName(name)";
        try {
            engine.put("ClientAPI", clientAPI);
            engine.put("name", "nameM");

            engine.eval(script);
        } catch (ScriptException e) {
            log.error("Script "+script +" exeption");
        }

        Thread startCLIThread = new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String command = scanner.nextLine().trim();
                if (command.equalsIgnoreCase("help")) {
                    log.info("help or h for help list");
                }else if (command.startsWith("player")){
                    ClientChat.clientChat("Player: "+Client.getPlayer());
                }else if (command.startsWith("api ")){
                    String apiCmd = "ClientAPI."+ command.substring(4);
                    try {
                        engine.eval(apiCmd);
                    } catch (ScriptException e) {
                        log.error("Error executing " +  apiCmd + e.getMessage());
                    }
                }else{
                    log.error("Invalid command " + command);
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
            log.error("Error client during stream creation " + e.getMessage() );
            return; //TODO
        }

        while(true){
            try {
                int packetType = objectInputStream.readInt();
                if (packetType == Packet.PacketType.CHAT.ordinal()){
                    ClientChat.clientChat( (String) objectInputStream.readObject());
                }if (packetType == Packet.PacketType.LOGIN.ordinal()){
                    Player.setPosition((Point) objectInputStream.readObject());
                    player.setEnergy(objectInputStream.readInt());
                }if (packetType == Packet.PacketType.CHUNK.ordinal()){
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
                        ClientAPI.canvasUpdate();
                    }
                }
            } catch(EOFException e) {
                continue;
            }catch (IOException | ClassNotFoundException e) {
                log.terror("Connection lost");
            }
        }
    }

    private class ClientChat{
        public static void clientChat(String msg){
            System.out.print("chat: " +msg+ "\n");
        }
    }
}