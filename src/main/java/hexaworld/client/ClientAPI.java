package hexaworld.client;

import hexaworld.CLog;
import hexaworld.geometry.Chunk;
import hexaworld.geometry.Geometry;
import hexaworld.geometry.Point;
import hexaworld.net.Packet;
import hexaworld.server.ServerAPI;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;

import java.io.*;
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
    for (int i = 0; i < Geometry.HEXAGON_BORDERS;i++){
      loadChunk(center.moveToNearChunk(i));
      //System.out.println("center.moveToNearChunk(i)" + center.moveToNearChunk(i));
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

  public static void canvasUpdate() {
    //TODO this is noly follow player mode
    //Player.getPosition().setTo(0,0);

    /*
    for (Chunk chunk : Client.getChunks()) {
      System.out.println("drawing chunk" +chunk.getPosition());
      chunk.draw(Point.minus(chunk.getPosition(), Player.getPosition()));
    }*/
  }
}
