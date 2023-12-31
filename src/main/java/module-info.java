module hexaworld {
  requires javafx.fxml;
  requires javafx.controls;
  requires java.scripting;
  requires java.sql;
  requires lombok;
  requires commons.cli;
  opens hexaworld to javafx.graphics;
  exports hexaworld;
  exports hexaworld.client;
  exports hexaworld.geometry;
  exports tests.example.hexaworld;
  exports hexaworld.cli;
  opens hexaworld.cli to javafx.graphics;
}