package hexaworld.geometry;

import lombok.Getter;

import java.io.Serializable;
@Getter
public class Point implements Serializable {
  public enum HEXA_MOVE{UP,RIGHT_UP, RIGHT_DOWN,DOWN,LEFT_DOWN,LEFT_UP}
  private double x,y;

  public Point(double x, double y){
    this.x=x;
    this.y=y;
  }

  public Point addPoint(int x, int y) {
    return new Point(this.x + x, this.y + y);
  }
  public Point addPoint(HEXA_MOVE move) {
    Point p = new Point(0,0);
    p.add(move);
    return p;
  }
  public Point getNextChunk(int border) {
    Point p = new Point(0,0);
    switch (border){
      case 1 -> p.add(2,6);
      case 2 -> p.add(4,0);
      case 3 -> p.add(2,-6);
      case 4 -> p.add(-2,-6);
      case 5 -> p.add(-4,0);
      case 6 -> p.add(-2,6);
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
  public void add(HEXA_MOVE move){
    switch (move){
      case UP -> y+=4;
      case RIGHT_UP -> {x++;y++;}
      case RIGHT_DOWN -> {x++;y--;}
      case DOWN -> y-=4;
      case LEFT_DOWN -> {x--;y--;}
      case LEFT_UP -> {x--;y++;}
    }
  }
  @Override
  public String toString() {
    return "[" + x +";"+y +"]";
  }
  public Point clonePoint() {
    return new Point(x, y);
  }
}
