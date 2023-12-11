package hexaworld.geometry;

import javafx.scene.shape.ClosePath;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;


public class Geometry {
  //hexagon
  public static final int HEXAGON_BORDERS = 6;

  public static Path createHexagonPath(double centerX, double centerY, double size) {
    Path hexagonPath = new Path();

    double h = getTriangleV(size);//height of triangle
    hexagonPath.getElements().add(new MoveTo(centerX, centerY-size)); //up
    hexagonPath.getElements().add(new LineTo(centerX+h, centerY-size/2));
    hexagonPath.getElements().add(new LineTo(centerX+h, centerY+size/2));
    hexagonPath.getElements().add(new LineTo(centerX, centerY+size));
    hexagonPath.getElements().add(new LineTo(centerX-h, centerY+size/2));
    hexagonPath.getElements().add(new LineTo(centerX-h, centerY-size/2));


    hexagonPath.getElements().add(new ClosePath());

    return hexagonPath;
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
}
