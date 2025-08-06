package edu.duke.ece651.simulationserver;

public class CoordinateSetter {
  // Static variables initialized to -1
  public static int maxRow = -1;
  public static int maxCol = -1;

  // Static method to set maxRow
  public static void setMaxRow(int row) {
    maxRow = row;
  }

  // Static method to set maxCol
  public static void setMaxCol(int col) {
    maxCol = col;
  }

  public static void setMax(Building b) {
    maxRow = Math.max(maxRow, b.getCoordinate().getRow());
    maxCol = Math.max(maxCol, b.getCoordinate().getColumn());
  }
  
  public static void setCoordinate(Building b) {
    if (maxRow == -1 && maxCol == -1) {
      maxRow = 0;
      maxCol = 0;
    } else {
      maxRow = maxRow + 4;
      maxCol = maxCol + 4;
    }
    b.setCoordinate(new Coordinate(maxRow, maxCol));
  }

  public static void reset() {
    maxRow = -1;
    maxCol = -1;
  }
}
