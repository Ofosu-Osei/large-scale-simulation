package edu.duke.ece651.simulationserver;

import java.util.List;
import java.util.Map;

/**
 * The SimplelatPolicy class is an implementation of the SourceSelectionPolicy
 * interface that selects a source building based on the sum of latencies of its queued requests.
 * 
 * For each candidate building that can produce the specified ingredient, this policy calculates the
 * "simple latency" as the total latency of all requests in its queue (using Building).
 * The candidate with the smallest sum is chosen as the source. In addition, the policy populates the provided
 * hooseStandard map with each candidate's name and its calculated simple latency.
 * 
 */
public class SimplelatPolicy implements SourceSelectionPolicy {
  /**
   * Selects the source building from the provided list of candidates that can produce the given ingredient,
   * based on the lowest total latency of queued requests.
   *
   * @param sources       the list of candidate buildings to select from
   * @param ingredient    the ingredient required
   * @param chooseStandard a map that will be populated with the candidate building names and their simple latency values
   * @return the building with the smallest total latency that can produce the specified ingredient, or {@code null}
   *         if no candidate building can produce it
   */
  @Override
  public Building selectSource(Map<Building, GraphPath> sources, String ingredient,Map<String,Integer> chooseStandard) {
    Building selected = null;
    int bestLatency = Integer.MAX_VALUE;
    for (Map.Entry<Building, GraphPath> entry : sources.entrySet()) {
      // if (entry.getValue() == null) continue;
      Building b = entry.getKey();   
      if (b.capableOf(ingredient) == null) {
        int simplelat=b.getSimplelat();
        chooseStandard.put(b.getName(), simplelat);
         if (simplelat < bestLatency) {
           bestLatency = simplelat;
           selected = b;
         }
      }
    }
    return selected;
  }
  
  /**
   * Returns the name of this source selection policy.
   *
   * @return a string representing the policy name
   */
  @Override
  public String getSourcePolicyName(){
    String s="simpleLat";
    return s;
  }
}
