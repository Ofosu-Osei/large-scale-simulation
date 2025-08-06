package edu.duke.ece651.simulationserver;

public class Square {
  protected Coordinate coordinate;

  // Constructor
  public Square(Coordinate coordinate) {
    this.coordinate = coordinate;
  }

  // Getter
  public Coordinate getCoordinate() {
    return coordinate;
  }

  // Setter
  public void setCoordinate(Coordinate coordinate) {
    this.coordinate = coordinate;
  }
}
