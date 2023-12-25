package tests.example.hexaworld;

import hexaworld.client.Client;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.stage.Stage;

public class CanvasView extends Application {

  private static final double CHUNK_SIZE = 50;

  @Override
  public void start(Stage primaryStage) {
    Pane root = new Pane();

    // Create a hexagon path
    Path hexagonPath = createHexagonPath(CHUNK_SIZE);
    hexagonPath.setFill(Color.GREEN);

    // Add hexagonPath to the root
    root.getChildren().add(hexagonPath);

    // Create a custom canvas on the hexagon shape

    primaryStage.setScene(new Scene(root, 300, 300));

    Canvas canvas = createHexagonCanvas(hexagonPath);
    GraphicsContext gc = canvas.getGraphicsContext2D();
    gc.setFill(Color.rgb(173, 216, 230, 0.5));
    gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

    root.getChildren().add(canvas);

    primaryStage.show();
  }

  private Path createHexagonPath(double size) {
    double centerX = size * 3 / 2;
    double centerY = size * Math.sqrt(3);

    Path hexagonPath = new Path();
    hexagonPath.getElements().addAll(
            new MoveTo(centerX + size, centerY),
            new LineTo(centerX + size / 2, centerY - size * Math.sqrt(3) / 2),
            new LineTo(centerX - size / 2, centerY - size * Math.sqrt(3) / 2),
            new LineTo(centerX - size, centerY),
            new LineTo(centerX - size / 2, centerY + size * Math.sqrt(3) / 2),
            new LineTo(centerX + size / 2, centerY + size * Math.sqrt(3) / 2),
            new ClosePath()
    );

    return hexagonPath;
  }
  private Canvas createHexagonCanvas(Path hexagonPath) {

    Canvas canvas = new Canvas(200, 200);

    GraphicsContext gc = canvas.getGraphicsContext2D();
    gc.setStroke(Color.BLACK);
    gc.setFill(Color.BLUE); // Set the fill color for the canvas

    double[] xPoints = {0, 50, 100, 100, 50, 0};
    double[] yPoints = {50 * Math.sqrt(3), 0, 0, 50 * Math.sqrt(3) * 2, 50 * Math.sqrt(3) * 2, 50 * Math.sqrt(3)};

    // Draw on the canvas using hexagon coordinates
    gc.fillPolygon(xPoints, yPoints, 6);
    gc.strokePolygon(xPoints, yPoints, 6);

    return canvas;
  }

  public static void main(String[] args) {
    launch(args);
  }
}
