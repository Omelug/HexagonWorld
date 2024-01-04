package hexaworld.server;

import hexaworld.CLog;
import hexaworld.client.Client;
import hexaworld.geometry.Chunk;
import hexaworld.geometry.Geometry;
import hexaworld.geometry.Point;
import hexaworld.net.Packet;
import hexaworld.net.TCPReceiver;
import lombok.Getter;

import java.io.*;
import java.net.Socket;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static hexaworld.net.Packet.PacketType.*;
import static hexaworld.server.ServerAPI.COMMAND.*;

public class ServerPlayer implements TCPReceiver {
  static private final CLog log = new CLog(CLog.ConsoleColors.BLUE);

  @Getter
  private String name = null;
  @Getter
  private Point position = new Point(0,0);
  @Getter
  private int energy = ServerConfig.getEnergyTable().get(CHANGE_NAME)+30;//TODO +50 for testing

  private List<Change> tickChanges;
  @Getter
  private final Socket clientSocket;
  @Getter
  private ObjectOutputStream objectOutputStream;
  private Set<Chunk> visibleChunks = new HashSet<>();

  public ServerPlayer(Socket clientSocket){
    this.clientSocket = clientSocket;
    Thread tcpStart = new Thread(() -> receiveTCP(clientSocket));
    tcpStart.start();
  }
  boolean payForCmd(int command, ServerAPI.COMMAND testedCmd){ //TODO only command without waiting
    int need = ServerConfig.getEnergyTable().get(testedCmd);
    if (command == testedCmd.ordinal()){
      if (need <= energy){
        energy -= need;
        return true;
      }else{
        Chat.msg(this,  CLog.ConsoleColors.RED+ "Not enough energy need " + need+". You have " + energy +"."+ CLog.ConsoleColors.RESET);
      }
    }
    return false;
  }
  @Override
  public void receiveTCP(Socket tcpClientSocket) {
    InputStream inputStream;
    ObjectInputStream objectInputStream;

    try {
      inputStream = tcpClientSocket.getInputStream();
      objectInputStream = new ObjectInputStream(inputStream);
      objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
    } catch (IOException e){
      log.terror("Error during stream creation");
      return;
    }


    while(true){
      try {
        int packetType = objectInputStream.readInt();
        System.out.print("  "+packetType +".");
          if (packetType == COMMAND.ordinal()){
            int command = objectInputStream.readInt();
            System.out.print(command+ ":");

            if (payForCmd(command,CHANGE_NAME)) {
              String newUsername = (String) objectInputStream.readObject();
              if (name == null){
                Chat.msgAll(CLog.ConsoleColors.GREEN + " + "+newUsername + " " + CLog.ConsoleColors.RESET);
              }else{
                Chat.msgAll(name + " changed name to " + newUsername);
              }
              name = newUsername;
            }else if (payForCmd(command,LOAD_CHUNK)) {

              Chunk chunk = Map.loadChunk((Point) objectInputStream.readObject());
              visibleChunks.add(chunk);

              objectOutputStream.writeInt(CHUNK.ordinal());
              objectOutputStream.writeObject(chunk);
              objectOutputStream.flush();
            }else if (command == MOVE.ordinal()) {

              //log.debug(position + " will be changed");

              Geometry.HEXA_MOVE move = (Geometry.HEXA_MOVE) objectInputStream.readObject();
              if (payForCmd(command, MOVE)){
                position.add(move);
                continue;
              }
              objectOutputStream.writeInt(CORRECTION.ordinal());
              objectOutputStream.writeInt(MOVE.ordinal());
              objectOutputStream.writeObject(move);

              //objectOutputStream.writeDouble(position.getX());
              //objectOutputStream.writeDouble(position.getY());
              objectOutputStream.flush();

              //log.debug("Sent packet content: " + getPacketContentAsString());

            }
          }else if (packetType == Packet.PacketType.LOGIN.ordinal()) {
            login();
          } else if (packetType == Packet.PacketType.CHAT.ordinal()) {
            Chat.msgAll("[" + name + "]" + (String) objectInputStream.readObject());
          }
      } catch(EOFException e) {
        log.debug(  "EOFException" + tcpClientSocket.isConnected());
        e.printStackTrace();
        kick();
        break;
      }catch (IOException | ClassNotFoundException e) {
        kick();
        break;
      }
    }
  }


  private String getPacketContentAsString() {
    try {
      // Create a temporary ByteArrayOutputStream to capture the binary data
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

      // Use another ObjectOutputStream for writing to the temporary stream
      ObjectOutputStream tempObjectOutputStream = new ObjectOutputStream(byteArrayOutputStream);

      // Write the same data to the temporary stream
      tempObjectOutputStream.writeInt(CORRECTION.ordinal());
      tempObjectOutputStream.writeInt(MOVE.ordinal());
      tempObjectOutputStream.writeObject(position);
      tempObjectOutputStream.flush();

      // Convert the binary data to a Base64-encoded string
      byte[] binaryData = byteArrayOutputStream.toByteArray();
      String base64Encoded = Base64.getEncoder().encodeToString(binaryData);

      // Close the temporary stream
      tempObjectOutputStream.close();

      return base64Encoded;
    } catch (IOException e) {
      // Handle exceptions
      e.printStackTrace();
      return null;
    }
  }

  private void tick(){
    try {
      objectOutputStream.writeInt(Packet.PacketType.TICK.ordinal());
      objectOutputStream.writeInt(energy);
      objectOutputStream.writeInt(tickChanges.size());
      for(Change tickChange : tickChanges) {
        objectOutputStream.writeObject(tickChange);
      }
      objectOutputStream.flush();
    } catch (IOException e) {
      log.error("TICK error");
    }
  }

  private void login() {
    try {
      objectOutputStream.writeInt(Packet.PacketType.LOGIN.ordinal());
      objectOutputStream.writeObject(position);
      objectOutputStream.writeInt(energy);
      objectOutputStream.flush();
    } catch (IOException e) {
      Client.log.error("Error LOGIN connection " + e.getMessage());
    }
  }

  public void kick() {
    Chat.msg(this,"You are kicking out");
    try {
      clientSocket.close();
    } catch (IOException e) {
     log.error("Client socket close exception" + e.getMessage());
    }
    Server.getPlayers().remove(this);
  }

  private class Change{
    public Change(){

    }
  }
}
