package edu.duke.ece651.simulationserver;
public class Coordinate {
  private final Integer row;
  private final Integer column;
  /**
   * Initial the Coordinate
   *
   * @return Initial the Coordinate
   */
  public Coordinate(Integer row,Integer column){
    this.row=row;
    this.column=column;
  }

  // Getter methods
  /**
   * Returns the x of Coordinate
   *
   * @return the x of Coordinate
   */
  public Integer getRow() {
    return row;
  }
  /**
   * Returns the y of Coordinate
   *
   * @return the y of Coordinate
   */
  public Integer getColumn() {
    return column;
  }
    
  /**
   * equals method to compare two Coordinate objects
   */
  @Override
  public boolean equals(Object o) {
    if (o == null) return false;
    if (o.getClass().equals(getClass())) {
      Coordinate c = (Coordinate) o;
      return row == c.row && column == c.column;
    }
    return false;
  }
  /**
   * Returns the string representation of Coordinate
   *
   * @return the string representation of Coordinate
   */
  @Override
  public String toString() {
    return "("+row+", " + column+")";
  }
  
  @Override
  public int hashCode() {
    return toString().hashCode();
  }

  public double distanceTo(Coordinate c) {
    return Math.sqrt(Math.pow(row - c.row, 2) + Math.pow(column - c.column, 2));
  }
}
