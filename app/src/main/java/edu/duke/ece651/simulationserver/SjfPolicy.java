package edu.duke.ece651.simulationserver;

import java.util.Map;
import java.util.Queue;

/**
 * This class implements the Shortest Job First (SJF) request selection policy.
 * 
 * This policy selects, from the given queue, the request that is ready and has
 * the lowest latency.
 * If two requests have the same latency, the one that appears first in the
 * queue is selected.
 * 
 */
public class SjfPolicy implements RequestSelectionPolicy {

  /**
   * Selects the request from the provided queue that is ready and has the lowest latency.
   *
   * @param requests  the queue of pending requests
   * @param inventory a map representing the current available quantities of ingredients;
   *                  keys are ingredient names and values are their available quantities
   * @return the first ready Request with the smallest recipe latency, or {null if no request is ready
   */
  @Override
  public Request selectRequest(Queue<Request> requests, Map<String, Integer> inventory) {
    Request selectedRequest = null;
    int bestLatency = Integer.MAX_VALUE;

    for (Request request : requests){
      if (!request.isReady(inventory)){
        continue;
      }
      int latency = request.getRecipe().getLatency();
      if (selectedRequest == null || latency < bestLatency){
        selectedRequest = request;
        bestLatency = latency;
      }
      //we select the oldest request first when there is a tie.
    }
    return selectedRequest;
  }

  /**
   * Returns the name of this request selection policy.
   *
   * @return a String representing the name of the policy.
   */
  @Override
  public String getRequestPolicyName(){
    String s="sjf";
    return s;
  }
  
}
