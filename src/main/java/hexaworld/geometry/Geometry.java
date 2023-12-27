package hexaworld.geometry;

import hexaworld.CLog;
import hexaworld.client.Client;
import hexaworld.client.Player;
import javafx.scene.Node;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.*;
import lombok.AllArgsConstructor;
import lombok.Getter;


public class Geometry {
  private static final CLog log = new CLog(CLog.ConsoleColors.CYAN_BRIGHT);

  public static void drawHexagon(GraphicsContext gc, double centerX, double centerY, double size) {

    double[] xPoints = new double[HEXA_MOVE.values().length];
    double[] yPoints = new double[HEXA_MOVE.values().length];

    for (int i = 0; i < HEXA_MOVE.values().length; i++) {
      xPoints[i] = centerX + HEXA_MOVE.get(i).getX()*size;
      yPoints[i] = centerY + HEXA_MOVE.get(i).getY()*size;
    }


    Geometry.multiplyArray(xPoints, Client.getVIEW_UNIT().getX());
    Geometry.multiplyArray(yPoints, Client.getVIEW_UNIT().getY());

    Geometry.addArray(xPoints, Client.getRoot().getWidth()/2);
    Geometry.addArray(yPoints, Client.getRoot().getHeight()/2);

    Geometry.addArray(xPoints, Client.getShift().getX());
    Geometry.addArray(yPoints, Client.getShift().getY());

    gc.fillPolygon(xPoints, yPoints, HEXA_MOVE.values().length);
    gc.strokePolygon(xPoints, yPoints, HEXA_MOVE.values().length);
  }

  public static HEXA_MOVE rotate180(HEXA_MOVE move) {
    return HEXA_MOVE.get((move.ordinal()+HEXA_MOVE.values().length/2)%HEXA_MOVE.values().length);
  }

  @Getter
  @AllArgsConstructor
  public enum HEXA_MOVE{UP(0,-2),RIGHT_UP(1,-1), RIGHT_DOWN(1,1),DOWN(0,2),LEFT_DOWN(-1,1),LEFT_UP(-1,-1);
    final double x,y;

    public static HEXA_MOVE get(int ordinal) {
      return values()[ordinal%values().length];
    }
  }

  @Getter
  @AllArgsConstructor
  public enum HEXAGON_BORDERS{R_UP(1,-3),R(2,0), R_D(1,3),L_D(-1,3),L(-2,0), L_U(-1,-3);
    final double x,y;
    public static HEXAGON_BORDERS get(int ordinal) {
      int cislo = ordinal%values().length;
      if(cislo< 0){
        cislo = (ordinal+values().length)%values().length;
      } //TODO pokudd je to potom v minusu, hodit error
      return values()[cislo];
    }
  }

  public static Path createHexagonPath(double centerX, double centerY, double size) {
    Path hexagonPath = new Path();
    Point sizePoint = multiplePoint(Client.getVIEW_UNIT(),size);

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
  /**public static void moveNoPlayer(double deltaX, double deltaY) {
    for (Node node : Client.getRoot().getChildren()) {
      if (node != Player.getHexagonPath()) {
        node.setTranslateX(node.getTranslateX() + deltaX);
        node.setTranslateY(node.getTranslateY() + deltaY);
      }
    }
  }
  public static void moveNoPlayerR(HEXA_MOVE move) {
    moveNoPlayer(-move.getX()*Client.getVIEW_UNIT().getX(), -move.getY()*Client.getVIEW_UNIT().getY());
  }**/
  public static void multiplyArray(double[] array, double factor) {
    for (int i = 0; i < array.length; i++) {
      array[i] *= factor;
    }
  }
  public static void addArray(double[] array, double value) {
    for (int i = 0; i < array.length; i++) {
      array[i] += value;
    }
  }


}
