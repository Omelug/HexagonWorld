package hexaworld.server;

public class ServerAPI {
  public enum COMMAND{
    MOVE, CHANGE_NAME, LOAD_CHUNK;

    public static COMMAND get(int command) {
      return COMMAND.values()[command];
    }
  }
  public void say_hello(String name) {
    System.out.println("Hello, " + name + " from Python!");
  }
}
