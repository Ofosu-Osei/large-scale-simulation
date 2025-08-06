package edu.duke.ece651.simulationserver;

import java.util.Map;
import java.util.Queue;

/**
 * This class implements the First-In-First-Out (FIFO) request selection policy.
 *
 * This policy selects the first request in the queue that is ready to be processed
 * according to the current inventory.
 */

public class FifoPolicy implements RequestSelectionPolicy {

  // @Override
  // public Request selectRequest(Queue<Request> requests, Map<String, Integer> inventory) {
  //   for (Request request : requests) {
  //     if (request.isReady(inventory)) {
  //       return request;
  //     } else {
  //       return null;
  //     }
  //   }
    
  //   return null;
  // }
  
  @Override
  public Request selectRequest(Queue<Request> requests, Map<String, Integer> inventory) {
    for (Request request : requests) {
      return request;
    }
    
    return null;
  }
  
  @Override
  public String getRequestPolicyName(){
    String s="fifo";
    return s;
  }
}
