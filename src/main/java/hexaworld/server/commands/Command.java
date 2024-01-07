package hexaworld.server.commands;

import hexaworld.cli.CLog;
import hexaworld.geometry.Chunk;
import hexaworld.geometry.Geometry;
import hexaworld.geometry.Point;
import hexaworld.server.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import static hexaworld.net.Packet.PacketType.CHUNK;
import static hexaworld.net.Packet.PacketType.CORRECTION;
import static hexaworld.server.ServerAPI.COMMAND.MOVE;

public class Command {
  private static final CLog log = new CLog(CLog.ConsoleColors.WHITE);

  private final ServerAPI.COMMAND commandType;
  private ServerPlayer player;

  public Command(ServerPlayer player, ServerAPI.COMMAND commandType) {
    this.player = player;
    this.commandType = commandType;
  }

  public boolean payForCmd(){
    int need = ServerConfig.getCommandTable().get(commandType).getEnergy();
      if (need <= player.getEnergy()){
        player.setEnergy(player.getEnergy()-need);
        return true;
      }
      Chat.msg(player,  CLog.ConsoleColors.RED+ "Not enough energy need " + need+". You have " + player.getEnergy() +"."+ CLog.ConsoleColors.RESET);
      return false;
  }

  public void addToWaitList() {
    player.getWaitCmdList().add(this);
  }

  public void deny() {
    try {
      ObjectOutputStream objectOutputStream = player.getObjectOutputStream();
      objectOutputStream.writeInt(CORRECTION.ordinal());
      switch (commandType){
        case MOVE:
          objectOutputStream.writeInt(MOVE.ordinal());
          Geometry.HEXA_MOVE correction = (Geometry.HEXA_MOVE) objectList.get(0);

          objectOutputStream.writeObject(correction);
          break;
        default:
          log.debug("Denying is not implemented yet " + commandType);
      }
      objectOutputStream.flush();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }
  public void accept() {
    ObjectOutputStream objectOutputStream = player.getObjectOutputStream();
    try {
      switch (commandType) {
        case CHANGE_NAME:
          String newUsername;
          newUsername = (String) objectList.get(0);
          if (player.getName() == null) {
            Chat.msgAll(CLog.ConsoleColors.GREEN + " + " + newUsername + " " + CLog.ConsoleColors.RESET);
          } else {
            Chat.msgAll(player.getName() + " changed name to " + newUsername);
          }
          player.setName(newUsername);
          break;
        case LOAD_CHUNK:
          Chunk chunk = Map.loadChunk((Point) objectList.get(0));
          player.getVisibleChunks().add(chunk);

          objectOutputStream.writeInt(CHUNK.ordinal());
          objectOutputStream.writeObject(chunk);
          objectOutputStream.flush();
          break;
        case MOVE:
          Geometry.HEXA_MOVE move = (Geometry.HEXA_MOVE) objectList.get(0);
          player.getPosition().add(move);
          break;
      }
    //SEND ACCEPT
    } catch (IOException e) {

      throw new RuntimeException(e);
    }
  }

  public void run() {
    boolean freeChunks = (commandType == ServerAPI.COMMAND.LOAD_CHUNK) && player.getVisibleChunks().size() < ServerConfig.FREE_CHUNK;
    if (freeChunks || payForCmd()){
      //TODO no time consumig yet       player.setTickBlocker(player.getTickBlocker() + ServerConfig.getCommandTable().get(commandType).getTime());
      accept();
    }else{
      deny();
    };
  }
  private List<Object> objectList = new ArrayList<>();

  public void saveInput(ObjectInputStream objectInputStream) {
    try {
      for (int i = 0; i < ServerConfig.getCommandTable().get(commandType).getReceiveObjCnt(); i++) {
        objectList.add(objectInputStream.readObject());
      }
    } catch (IOException | ClassNotFoundException e) {
        throw new RuntimeException(e);
    }
  }
}
