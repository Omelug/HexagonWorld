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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ServerPlayer implements TCPReceiver {
  static private final CLog log = new CLog(CLog.ConsoleColors.BLUE);

  @Getter
  private String name = null;
  @Getter
  private Point position = new Point(0,0);
  @Getter
  private int energy = ServerConfig.getEnergyTable().get(ServerAPI.COMMAND.CHANGE_NAME)+50;//TODO +50 for testing

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
  boolean payForCmd(int command, ServerAPI.COMMAND testedCmd){ //TOD only command without waiting
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
      log.error("Error during stream creation");
      return; //TODO
    }


    while(true){
      try {
        int packetType = objectInputStream.readInt();
        System.out.print("  "+packetType +".");
          if (packetType == Packet.PacketType.COMMAND.ordinal()){
            int command = objectInputStream.readInt();
            System.out.print(command+ ":");

            if (payForCmd(command,ServerAPI.COMMAND.CHANGE_NAME)) {
              String newUsername = (String) objectInputStream.readObject();
              if (name == null){
                Chat.msgAll(CLog.ConsoleColors.GREEN + " + "+newUsername + " " + CLog.ConsoleColors.RESET);
              }else{
                Chat.msgAll(name + " changed name to " + newUsername);
              }
              name = newUsername;
            }
            if (payForCmd(command,ServerAPI.COMMAND.LOAD_CHUNK)) {
              Point chunkCenter = (Point) objectInputStream.readObject();

              Chunk chunk = Map.loadChunk(chunkCenter);
              visibleChunks.add(chunk);

              objectOutputStream.writeInt(Packet.PacketType.CHUNK.ordinal());
              objectOutputStream.writeObject(chunk);
              objectOutputStream.flush();
            }
          }if (packetType == Packet.PacketType.LOGIN.ordinal()) {
            login();
          }
      } catch(EOFException e) {
        continue;
      }catch (IOException | ClassNotFoundException e) {
        throw new RuntimeException(e);
      }
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
  private class Change{
    public Change(){

    }
  }
}
