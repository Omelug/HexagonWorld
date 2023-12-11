package hexaworld.tests;

import hexaworld.CLog;
import hexaworld.geometry.Geometry;
import hexaworld.geometry.Point;
import javafx.scene.paint.Color;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static hexaworld.geometry.Geometry.posInChunk;

public class GeometryTests {
  @AllArgsConstructor
  public static class Test{
    public static void testPosInChunk(Point data, Point correct) {
      Point result = posInChunk(data);
      if(Point.same(result,correct)){
        System.out.println(CLog.ConsoleColors.GREEN + " PASSED" + CLog.ConsoleColors.RESET);
      }else{
        System.out.println(CLog.ConsoleColors.RED + "FAILED " + data +" -> " + result+ "should be "+ correct + CLog.ConsoleColors.RESET);
      }
    }
  }

  public static void main(String[] args) {
    Test.testPosInChunk(new Point(1,2), null);
    Test.testPosInChunk(new Point(1,3), new Point(-1,-3));
    Test.testPosInChunk(new Point(-1,-3), new Point(-1,-3));
  }
}
