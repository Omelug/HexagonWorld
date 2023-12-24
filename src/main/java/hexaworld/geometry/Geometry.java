package hexaworld.geometry;

import hexaworld.CLog;
import hexaworld.client.Client;
import hexaworld.client.Player;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;


public class Geometry {

  //hexagon
  //public static final int HEXAGON_BORDERS = 6;
  @AllArgsConstructor
  public enum HEXA_MOVE{UP(0,-2),RIGHT_UP(1,-1), RIGHT_DOWN(1,1),DOWN(0,2),LEFT_DOWN(-1,1),LEFT_UP(-1,-1);
    @Getter
    final double x,y;
  }
  public enum HEXAGON_BORDERS{R_UP,R, R_D,L_D,L, L_U}

  private static CLog log = new CLog(CLog.ConsoleColors.CYAN_BRIGHT);

  public static Path createHexagonPath(double centerX, double centerY, double size) {
    Path hexagonPath = new Path();
    Point sizePoint = multiplePoint(Client.getVIEW_UNIT(),size); //TODO
    //System.out.println("sizePoint " + sizePoint);

    centerX *= Client.getVIEW_UNIT().getX();
    centerY *= Client.getVIEW_UNIT().getY();

    hexagonPath.getElements().add(new MoveTo(centerX, centerY-2*sizePoint.getY())); //log.debug("createHexagonPath " + centerX + ";" + centerY); log.debug(" " + centerX*sizePoint.getX() + ";" + (centerY-2)*sizePoint.getY());

    hexagonPath.getElements().add(new LineTo(centerX+sizePoint.getX(), centerY-sizePoint.getY()));
    hexagonPath.getElements().add(new LineTo(centerX+sizePoint.getX(), centerY+sizePoint.getY()));
    hexagonPath.getElements().add(new LineTo(centerX, centerY+2*sizePoint.getY()));
    hexagonPath.getElements().add(new LineTo(centerX-sizePoint.getX(), centerY+sizePoint.getY()));
    hexagonPath.getElements().add(new LineTo(centerX-sizePoint.getX(), centerY-sizePoint.getY()));


    hexagonPath.getElements().add(new ClosePath());

    return hexagonPath;
  }

  private static Point multiplePoint(Point point, double c) {
    return new Point(point.getX()*c, point.getY()*c);
  }

  public static int getTriangleV(double size) {
    return (int) Math.sqrt(Math.pow(size,2)-Math.pow(size/2,2)); //TODO double or int>
  }

  public static boolean isCenterHF(Point point) {
    if ((point.getX()%4 == 0 && (point.getY()%8 == 0)) || ((point.getX()+2)%4 == 0 && ((point.getY()+6)%12 == 0))){
      System.out.println("Point " + point+ " is center");
      return true;
    }
    return false;
  }
  public static Point posInChunk(Point point) {
    int[] x = {0,0,-1,-1,0,1,1,0,-1,-2,-2,-2,-1};
    int[] y = {0,-2,-1,1,2,1,-1,-4,-3,-2,0,2,3};

    for(int i = 0;i <12;i++){ //12 is count of unique position in chunk
      if (isCenterHF(point.addPoint(x[i],y[i]))){ return new Point(x[i],y[i]);}
    }

    System.out.println("ERROR invalid point");
    return null;
  }
  public static void moveAllChildren(double deltaX, double deltaY) {

    for (Node node : Client.getRoot().getChildren()) {
      node.setTranslateX(node.getTranslateX() + deltaX);
      node.setTranslateY(node.getTranslateY() + deltaY);
    }
  }
  public static void moveAllChildren(HEXA_MOVE move) {
    moveAllChildren(move.getX(), move.getY());
  }
  public static void moveNoPlayer(double deltaX, double deltaY) {
    for (Node node : Client.getRoot().getChildren()) {
      if (node != Player.getHexagonPath()) {
        node.setTranslateX(node.getTranslateX() + deltaX);
        node.setTranslateY(node.getTranslateY() + deltaY);
      }
    }
  }
  public static void moveNoPlayerR(HEXA_MOVE move) {
    moveNoPlayer(-move.getX()*Client.getVIEW_UNIT().getX(), -move.getY()*Client.getVIEW_UNIT().getY());
  }

}
