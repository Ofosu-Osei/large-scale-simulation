package edu.duke.ece651.simulationserver;

import java.util.Map;
import java.util.Queue;

/**
 * This is an interface for selecting the next production
 * Request from a queue based on a specific policy.
 */
public interface RequestSelectionPolicy {
  /**
   * Selects an appropriate request from the provided queue, possibly taking into
   * account the
   * building's inventory or other criteria.
   *
   * @param requests  the queue of pending requests
   * @param inventory the inventory of the building (this param can be omitted
   *                  dependending on how the implemetation goes)
   * @return the selected request, or null if none is eligible
   */
  Request selectRequest(Queue<Request> requests, Map<String, Integer> inventory);
  String getRequestPolicyName();
}
