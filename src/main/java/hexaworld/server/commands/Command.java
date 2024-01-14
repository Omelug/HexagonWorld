package hexaworld.server.commands;

import hexaworld.cli.CLog;
import hexaworld.geometry.Chunk;
import hexaworld.geometry.Geometry;
import hexaworld.geometry.Point;
import hexaworld.net.Change;
import hexaworld.server.*;
import lombok.Getter;

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
  @Getter
  private final ServerPlayer player;

  public Command(ServerPlayer player, ServerAPI.COMMAND commandType) {
    this.player = player;
    this.commandType = commandType;
  }

  public boolean payForCmd(){
    int need = ServerConfig.getCommandTable().get(commandType).getEnergy();
      if (need <= player.getEnergy()){
        player.setEnergy(player.getEnergy()-need);
        player.addChange(Change.CHANGE.ENERGY);
        return true;
      }
      Chat.msg(player,  CLog.ConsoleColors.RED+ "Not enough energy need " + need+". You have " + player.getEnergy() +"."+ CLog.ConsoleColors.RESET);
      return false;
  }

  public void deny() {
    try {
      ObjectOutputStream objectOutputStream = player.getObjectOutputStream();
      switch (commandType){
        case MOVE:
          objectOutputStream.writeObject(CORRECTION);
          objectOutputStream.writeObject(MOVE);
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
    player.getWaitCmdList().remove( player.getWaitCmdList().size()-1);
  }
  public void accept() {
    ObjectOutputStream objectOutputStream = player.getObjectOutputStream();
    try {
      switch (commandType) {
        case CHANGE_NAME:
          String newUsername;
          newUsername = (String) objectList.get(0);
          if(Server.getPlayerByName(newUsername) != null){
            Chat.msg(player, "User with name " + newUsername + " already exist");
            deny();
            break;
          }
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

          objectOutputStream.writeObject(CHUNK);
          objectOutputStream.writeObject(chunk);
          objectOutputStream.flush();
          break;
        case MOVE:
          Geometry.HEXA_MOVE move = (Geometry.HEXA_MOVE) objectList.get(0);
          player.getPosition().add(move);
          player.addChange(Change.CHANGE.POSITION);
          break;
      }
    //SEND ACCEPT ????
    } catch (IOException e) {
      log.error( commandType + " accept IOException " + e.getMessage());
    }finally {
      player.getWaitCmdList().remove( player.getWaitCmdList().size()-1);
    }
  }

  public void run() {
    boolean freeChunks = (commandType == ServerAPI.COMMAND.LOAD_CHUNK) && player.getVisibleChunks().size() < ServerConfig.FREE_CHUNK;
    if (freeChunks || payForCmd()){
      log.debug("accept " + commandType);
      player.setTickBlocker(player.getTickBlocker() + ServerConfig.getCommandTable().get(commandType).getTime());
      accept();
    }else{
      log.debug("deny " + commandType);
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

  public void addToWaitList() {
    player.getWaitCmdList().add(this);
  }
  @Override
  public String toString(){
    return commandType.name();
  }
}
