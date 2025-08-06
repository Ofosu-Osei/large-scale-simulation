package edu.duke.ece651.simulationserver;

import java.util.Map;

/**
 * Abstract class for placement rule checking.
 */
public abstract class PlacementRuleChecker {
  private final PlacementRuleChecker next;

  protected abstract String checkMyRule(Square theSquare, Map<Coordinate, Square> squares);
  
  /**
   * constructs a placememnt rule checker.
   *
   * @param next is the next placement rule to be checked,
   */
  public PlacementRuleChecker(PlacementRuleChecker next) {
    this.next = next;
  }

  /**
   * Check if the ship can be place
   */
  public String checkPlacement (Square theSquare, Map<Coordinate, Square> squares) {
    //if we fail our own rule: stop the placement is not legal
    String result = checkMyRule(theSquare, squares);
    if (result != null) {
      return result;
    }
    //other wise, ask the rest of the chain.
    if (next != null) {
      return next.checkPlacement(theSquare, squares);
    }
    //if there are no more rules, then the placement is legal
    return null;
  }

}


