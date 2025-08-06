package edu.duke.ece651.simulationserver;

import java.util.List;
import java.util.Map;

/**
 * The QlenPolicy class is an implementation of the SourceSelectionPolicy
 * interface that selects a source building based on the number of pending requests in its queue.
 * 
 * Specifically, it iterates over the candidate buildings and, for each building that can produce
 * the specified ingredient, it compares the number of requests in its queue. The building with the
 * smallest queue length is selected as the source. In the event of a tie, the building that appears
 * first in the provided list is chosen.
 */
public class QlenPolicy implements SourceSelectionPolicy {
 
 /**
   * Selects the source building from a list of candidate buildings based on the smallest queue length.
   *
   * @param sources the list of candidate buildings
   * @param ingredient the name of the ingredient to be produced
   * @param chooseStandard a map that will be populated with candidate building names as keys and their
   *                       current request queue lengths as values
   * @return the building with the fewest requests that can produce the given ingredient, or null
   *         if no such building exists
   */
  @Override
  public Building selectSource(Map<Building, GraphPath> sources, String ingredient,Map<String,Integer> chooseStandard) {
    Building selected = null;
    int minRequests = Integer.MAX_VALUE;
    for (Map.Entry<Building, GraphPath> entry : sources.entrySet()) {
      Building b = entry.getKey();
      if (b.capableOf(ingredient) == null) {
        int qlen = b.getQlen();
        chooseStandard.put(b.getName(), qlen);
        if (qlen < minRequests) {
          minRequests = qlen;
          selected = b;
        }
      }
    }
    return selected;
  }

  /**
   * Returns the name of this source selection policy.
   *
   * @return a string representing this policy
   */
  @Override
  public String getSourcePolicyName(){
    String s="qlen";
    return s;
  }
}
