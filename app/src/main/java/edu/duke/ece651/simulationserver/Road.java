package edu.duke.ece651.simulationserver;
public class Road extends Square{
  private int[] direction;
  
  private static int[] convertDirection(String dir) {
    if (dir == null) return null;
    
    switch (dir.toUpperCase()) {
    case "U": return new int[]{1, 0};
    case "D": return new int[]{-1, 0};
    case "L": return new int[]{0, -1};
    case "R": return new int[]{0, 1};
    default:
      throw new IllegalArgumentException("Invalid direction: " + dir);
    }
  }

  public Road(Coordinate coordinate, String directionStr) {
    super(coordinate);
    this.direction = convertDirection(directionStr);
  }

  public Road(Coordinate coordinate) {
    super(coordinate);
    this.direction = null;
  }

  
  public Road(Coordinate coordinate, int[] direction) {
    super(coordinate);
    if (!isValidDirection(direction)) {
      throw new IllegalArgumentException("Invalid direction!");
    }
    this.direction = direction;
  }

  private static boolean isValidDirection(int[] dir) {
    if (dir == null) return true;
    if (dir.length != 2) return false;

    return (dir[0] == 0 && dir[1] == 1) ||  // U
      (dir[0] == 0 && dir[1] == -1) || // D
      (dir[0] == -1 && dir[1] == 0) || // L
      (dir[0] == 1 && dir[1] == 0);    // R
  }
  
  // Getter 和 Setter
  public Coordinate getCoordinate() {
    return coordinate;
  }

  public void setCoordinate(Coordinate coordinate) {
    this.coordinate = coordinate;
  }

  public int[] getDirection() {
    return direction;
  }

  public void setDirection(String direction) {
    this.direction = convertDirection(direction);
  }

  
  @Override
  public String toString() {
    String dirStr = null;
    if (direction != null) {
      dirStr = "(" + direction[0] + ", " + direction[1] + ")";
    }
    return "Location: " + coordinate + ", direction: " + dirStr;
  }
}

