package hexaworld.client;

import hexaworld.geometry.Chunk;
import hexaworld.geometry.Geometry;
import hexaworld.geometry.Point;
import hexaworld.net.Packet;
import hexaworld.server.ServerAPI;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import static hexaworld.geometry.Geometry.posInChunk;

public class ClientAPI {

  private static ObjectOutputStream objectOutputStream;

  public static void init(){
    try {
      OutputStream outputStream = Client.getTcpSocket().getOutputStream();
      objectOutputStream = new ObjectOutputStream(outputStream);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
  public static void end(){
    try {
      objectOutputStream.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static void changeName(String username) {
    try{
      objectOutputStream.writeInt(Packet.PacketType.COMMAND.ordinal());
      objectOutputStream.writeInt(ServerAPI.COMMAND.CHANGE_NAME.ordinal());
      objectOutputStream.writeObject(username);

      objectOutputStream.flush();
    } catch (IOException e) {
      Client.log.error("Error TCP connection " + e.getMessage());
    }
  }

  public static void loadAround() {
    Point center = getChunkCenter(Player.getPosition());
    loadChunk(center);
    for (Geometry.HEXAGON_BORDERS border: Geometry.HEXAGON_BORDERS.values()){
      loadChunk(center.moveToNearChunk(border));
    }
  }

  private static Point getChunkCenter(Point position) {
    Point result = position.clonePoint();
    result.add(Objects.requireNonNull(posInChunk(position)));
    return result;
  }

  public static void loadChunk(Point centerOfChunk) { //center point
    try{
      objectOutputStream.writeInt(Packet.PacketType.COMMAND.ordinal());
      objectOutputStream.writeInt(ServerAPI.COMMAND.LOAD_CHUNK.ordinal());
      objectOutputStream.writeObject(centerOfChunk);
      objectOutputStream.flush();
    } catch (IOException e) {
      Client.log.error("Error loadChunk " + e.getMessage());
    }
  }

  public static void login() {
    try {
      objectOutputStream.writeInt(Packet.PacketType.LOGIN.ordinal());
    } catch (IOException e) {
      Client.log.error("Error TCP connection " + e.getMessage());
    }
  }

  public static void mapCanvasUpdate() {
    Client.getRoot().getChildren().remove(Client.getMapCanvas());

    Client.setMapCanvas(new Canvas(Client.getRoot().getWidth(),Client.getRoot().getHeight()));
    GraphicsContext gc = Client.getMapCanvas().getGraphicsContext2D();
    gc.setFill(Color.rgb(200, 15, 230, 0.5));
    gc.fillRect(0, 0, Client.getMapCanvas().getWidth(), Client.getMapCanvas().getHeight());

    List<Chunk> chunks = Client.getChunks();
    Iterator<Chunk> iterator = chunks.iterator();

    while (iterator.hasNext()) {
      Chunk chunk = iterator.next();
      chunk.draw();
    }

    Chunk.drawTriangles(gc);


    Client.getRoot().getChildren().add(Client.getMapCanvas());
  }

  public static void playerCanvasUpdate() {
    Client.getRoot().getChildren().remove(Client.getPlayerCanvas());
    Client.setPlayerCanvas(new Canvas(Client.getRoot().getWidth(),Client.getRoot().getHeight()));
    GraphicsContext gc = Client.getPlayerCanvas().getGraphicsContext2D();
    gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
    gc.setFill(Color.TRANSPARENT);
    Player.draw(gc);
    Client.getRoot().getChildren().add(Client.getPlayerCanvas());
  }
  public static void canvasUpdate() {
    mapCanvasUpdate();
    playerCanvasUpdate();
  }

  public static void chat(String substring) {
    try{
      objectOutputStream.writeInt(Packet.PacketType.CHAT.ordinal());
      objectOutputStream.writeObject(substring);
      objectOutputStream.flush();
    } catch (IOException e) {
      Client.log.error("Error chat " + e.getMessage());
    }
  }
}
