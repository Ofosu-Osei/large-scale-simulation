package edu.duke.ece651.simulationserver;

import java.util.Map;

public class NoCollisionRuleChecker extends PlacementRuleChecker {
   /**
   * Constructs a check main.
   *
   * @param next is the next placement rule to be checked.
   */
  public NoCollisionRuleChecker(PlacementRuleChecker next) {
    super(next);
  }

  /**
   * check if the given ship can be placed on the board.
   *
   * @param theShip is the given ship.
   * @param theBoard is the given board.
   * @return true if it can be placed, not otherwise. 
   */
  @Override
  protected String checkMyRule (Square theSquare, Map<Coordinate, Square> squares) {
    // TODO Auto-generated method stub
    Coordinate coordinate = theSquare.getCoordinate(); 
    for (Square s : squares.values()) {
      if (coordinate != null && coordinate.equals(s.getCoordinate())) {
        return "That placement is invalid: the building overlaps another building.";
      }
    }
    
    return null;
  }

}
