package hexaworld.server;

import hexaworld.cli.CLog;
import hexaworld.client.Client;
import hexaworld.geometry.Chunk;
import hexaworld.geometry.Point;
import hexaworld.net.Packet;
import hexaworld.net.TCPReceiver;
import hexaworld.server.commands.Command;
import lombok.Getter;
import lombok.Setter;

import java.io.*;
import java.net.Socket;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static hexaworld.net.Packet.PacketType.COMMAND;
import static hexaworld.server.ServerAPI.COMMAND.CHANGE_NAME;


public class ServerPlayer implements TCPReceiver {
  static private final CLog log = new CLog(CLog.ConsoleColors.BLUE);


  @Getter @Setter
  private String name = null;
  @Getter @Setter
  private int tickBlocker = 0;
  @Getter
  private final Point position = new Point(0,0);
  @Getter @Setter
  private int energy = ServerConfig.getCommandTable().get(CHANGE_NAME).getEnergy()+30;//TODO +30 for testing
  @Getter @Setter
  private List<Command> waitCmdList;
  @Getter
  private final Socket clientSocket;
  @Getter
  private ObjectOutputStream objectOutputStream;
  @Getter @Setter
  private final Set<Chunk> visibleChunks = new HashSet<>();

  public ServerPlayer(Socket clientSocket){
    this.clientSocket = clientSocket;
    Thread tcpStart = new Thread(() -> receiveTCP(clientSocket));
    tcpStart.start();
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
        //log.debug("  "+packetType +".");
          if (packetType == COMMAND.ordinal()){
            int command = objectInputStream.readInt();
            //log.debug(command+ ":");
            Command cmd = new Command(this,ServerAPI.COMMAND.get(command));
            cmd.saveInput(objectInputStream);
            if (tickBlocker == 0){
              cmd.run();
            }else{
              //cmd.saveInput(objectInputStream);
              //cmd.addToWaitList();
              //TODO send WAIT
              log.debug("Waiting is not implemented yet, tickBlocker:" + tickBlocker);
            }
          }else if (packetType == Packet.PacketType.LOGIN.ordinal()) {
            login();
          } else if (packetType == Packet.PacketType.CHAT.ordinal()) {
            Chat.msgAll("[" + name + "]" + objectInputStream.readObject());
          }
      } catch(EOFException e) {
        log.info( "EOFException" + tcpClientSocket.isConnected());
        //e.printStackTrace();
        kick();
        break;
      }catch (IOException | ClassNotFoundException e) {
        log.info("Connection error " + e.getMessage());
        kick();
        break;
      }
    }
  }

  /*private void tick(){
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
  }*/

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
    Chat.msg(this, name + " kicking out");
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
