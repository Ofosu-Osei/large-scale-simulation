package edu.duke.ece651.simulationserver;

import java.util.Map;
import java.util.Queue;

/**
 * This class implements the ready request selection policy.
 * 
 * This policy selects, from the given queue, the request that is ready first.
 * 
 * 
 */
public class ReadyPolicy implements RequestSelectionPolicy {

  /**
   * Selects the first request from the provided queue that is ready to be processed.
   * 
   * @param requests  the queue of pending requests
   * @param inventory a map representing the current inventory of ingredients for the building;
   *                  keys are ingredient names and values are their available quantities
   * @return the first ready Request from the queue, or null if no request is ready
   */
  @Override
  public Request selectRequest(Queue<Request> requests, Map<String, Integer> inventory) {
    Request selectedRequest = null;
    //choose the first ready request
    for (Request request : requests){
        if (request.isReady(inventory)){
            selectedRequest = request;
            break;   
        }
    }
    return selectedRequest;
  }

  /**
   * Returns the name of this request selection policy.
   *
   * @return a string representing this policy
   */
  @Override
  public String getRequestPolicyName(){
    String s="ready";
    return s;
  }
  
}
