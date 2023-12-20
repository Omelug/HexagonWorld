package hexaworld.client;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Grid {
  public enum GridType{TRIANGLE}
  public Grid(GridType type, Canvas canvas) {
    if (type == GridType.TRIANGLE){
      GraphicsContext gc = canvas.getGraphicsContext2D();
      gc.setStroke(Color.GRAY);

      double gridSize = 30.0;

      double slope = Math.sqrt((double) 5 / 4);

      /*for (double x = 0; x < canvas.getWidth() + canvas.getHeight() / slope; x += gridSize * slope) {
        gc.strokeLine(x, 0, x - canvas.getHeight(), canvas.getHeight());
      }

      for (double x = -canvas.getHeight(); x < canvas.getWidth() + canvas.getHeight() / slope; x += gridSize * slope) {
        gc.strokeLine(x, 0, x + canvas.getWidth() + canvas.getHeight(), canvas.getHeight());
      }*/


      /*for (double x = 0; true; x++) {
        if (x*gridSize*2*Math.sqrt(2) < canvas.getWidth() || x*gridSize < canvas.getHeight() + gridSize ){
          gc.strokeLine(x*gridSize*2*Math.sqrt((double) 5 /4), 0, 0, x*gridSize);
        }else{
          break;
        }
      }*/

      /*for (double y = 0; true; y++) {
        if (y*gridSize*Math.sqrt(2) < canvas.getWidth() || y*gridSize < canvas.getHeight() + gridSize ){
          gc.strokeLine(y*gridSize*Math.sqrt((double) 5 /4), 0, 0, y*gridSize);
        }else{
          break;
        }
      }*/

    }else{
      System.out.println("ERROR: Invalid grid type");
    }
  }
}
