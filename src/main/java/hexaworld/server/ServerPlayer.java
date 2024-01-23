package hexaworld.server;

import hexaworld.cli.CLog;
import hexaworld.client.Client;
import hexaworld.geometry.Chunk;
import hexaworld.geometry.Point;
import hexaworld.net.Change;
import hexaworld.net.Packet;
import hexaworld.net.TCPReceiver;
import hexaworld.server.commands.Command;
import lombok.Getter;
import lombok.Setter;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static hexaworld.net.Packet.PacketType.COMMAND;
import static hexaworld.server.ServerAPI.COMMAND.CHANGE_NAME;
import static hexaworld.server.ServerConfig.PLAYER_WAIT_LIST_SIZE;


public class ServerPlayer implements TCPReceiver {
  static private final CLog log = new CLog(CLog.ConsoleColors.BLUE);


  @Getter @Setter
  private String name = "Player42";
  @Getter @Setter
  private int tickBlocker = 0;
  @Getter
  private final Point position = new Point(0,0);
  @Getter @Setter
  private int energy = ServerConfig.getCommandTable().get(CHANGE_NAME).getEnergy()+30;//TODO +30 for testing
  @Getter @Setter
  private List<Command> waitCmdList = new ArrayList<>();
  @Getter
  private final Socket clientSocket;
  @Getter
  private ObjectOutputStream objectOutputStream;
  @Getter @Setter
  private final Set<Chunk> visibleChunks = new HashSet<>();
  Set<Change.CHANGE> changeSet = new HashSet<>();

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
        Packet.PacketType packetType = (Packet.PacketType) objectInputStream.readObject();
        log.debug("  "+packetType +".");
          if (packetType == COMMAND){
            ServerAPI.COMMAND command = (ServerAPI.COMMAND) objectInputStream.readObject();
            //log.debug(command+ ":");
            Command cmd = new Command(this,command);
            cmd.saveInput(objectInputStream);
            if(PLAYER_WAIT_LIST_SIZE >= waitCmdList.size()){
              cmd.addToWaitList();
            }else{
              log.debug("Limit of commands " + PLAYER_WAIT_LIST_SIZE +" reached ");
              cmd.deny(false);
            }
          }else if (packetType == Packet.PacketType.LOGIN) {
            login();
          } else if (packetType == Packet.PacketType.CHAT) {
            Chat.msgAll("[" + name + "]" + objectInputStream.readObject());
          }
      } catch(EOFException e) {
        log.info( "EOFException " + tcpClientSocket.isConnected());
        //e.printStackTrace();
        kick();
        break;
      }catch (IOException | ClassNotFoundException e) {
        log.info("Connection error " + e.getMessage());
        e.printStackTrace();
        kick();
        break;
      }
    }
  }

  public void tick(){
      if(tickBlocker > 0) {tickBlocker--;}
      while (tickBlocker == 0 && !waitCmdList.isEmpty()){
        waitCmdList.get(0).run();
      }
  }

  public void sendTick(){
    try {
      if (objectOutputStream == null){
        log.debug( name +"'s output is out");
        return;
      }
      objectOutputStream.writeObject(Packet.PacketType.TICK);

      //TODO zpracovat changeSet
      if (changeSet.contains(Change.CHANGE.POSITION)){

        //log.debug(changeSet.toString());
        objectOutputStream.writeObject(Change.CHANGE.POSITION);
        objectOutputStream.writeDouble( position.getX());
        objectOutputStream.writeDouble( position.getY());
      }

      /*if (changeSet.contains(Change.CHANGE.ENERGY)){
        objectOutputStream.writeObject(Change.CHANGE.ENERGY);
        objectOutputStream.writeInt(energy);
      }*/
      objectOutputStream.writeObject(Change.CHANGE.STOP);

      objectOutputStream.flush();
    } catch (IOException e) {
      log.error("TICK error");
    }
    //reset useless data
    changeSet.clear();
  }

  private void login() {
    try {
      objectOutputStream.writeObject(Packet.PacketType.LOGIN);
      objectOutputStream.writeDouble(position.getX());
      objectOutputStream.writeDouble(position.getY());
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

  @Override
  public String toString(){
    return name +" ["+ clientSocket.getInetAddress() + ":" + clientSocket.getPort()+"]";
  }

  public void addChange(Change.CHANGE change) {
    if(!change.isHasData()) {
      changeSet.add(change);
    }
  }

  public void addEnergy(int i) {
    energy += i;
  }
}
