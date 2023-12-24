package hexaworld.geometry;

import lombok.Getter;

import java.io.Serializable;
@Getter
public class Point implements Serializable {
  public static Point minus(Point A, Point B) {
    return new Point(A.x - B.x, A.y - B.y);
  }
  public static Point plus(Point A, Point B) {
    return new Point(A.x + B.x, A.y + B.y);
  }

  public void setTo(double x, double y) {
    this.x = x;
    this.y = y;
  }

  private double x,y;

  public Point(double x, double y){
    this.x=x;
    this.y=y;
  }

  public Point addPoint(double x, double y) {
    return new Point(this.x + x, this.y + y);
  }
  public Point addPoint(Geometry.HEXA_MOVE move) {
    Point p = new Point(0,0);
    p.add(move);
    return p;
  }
  public Point moveToNearChunk(Geometry.HEXAGON_BORDERS border) {
    Point p = clonePoint();
    switch (border){
      case R_UP -> p.add(2,6);
      case R -> p.add(4,0);
      case R_D -> p.add(2,-6);
      case L_D -> p.add(-2,-6);
      case L -> p.add(-4,0);
      case L_U -> p.add(-2,6);
    }
    return p;
  }

  public void add(double deltaX, double deltaY) {
    x+=deltaX; y+=deltaY;
  }
  public void add(Point point) {
    x+= point.getX();
    y+= point.getY();
  }
  public static boolean same(Point p1, Point p2) {
    if (p1 == null || p2 == null){
      return p1 == null && p2 == null;
    }
    return p1.getX() == p2.getX() && p1.getY() == p2.getY();
  }
  public void add(Geometry.HEXA_MOVE move){
    x += move.getX();
    y += move.getY();
  }
  @Override
  public String toString() {
    return "[" + x +";"+y +"]";
  }
  public Point clonePoint() {
    return new Point(x, y);
  }
}
