package edu.duke.ece651.simulationserver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SjfPolicyTest {
  private SjfPolicy sjfPolicy;
  private Queue<Request> requests;
  private Map<String, Integer> inventory;

  @BeforeEach
  public void setUp() {
    sjfPolicy = new SjfPolicy();
    requests = new LinkedList<>();
    inventory = new HashMap<>();
  }

  /**
   * When the queue is empty, selectRequest should returns null.
   */
  @Test
  public void test_emptyQueue() {
    Request selected = sjfPolicy.selectRequest(requests, inventory);
    assertNull(selected);
  }

  /**
   * A single ready request with no ingredient requirements should be selected.
   */
  @Test
  public void test_singleReadyRequest() {
    Recipe recipe1 = new Recipe("item1", new LinkedHashMap<>(), 10);
    Request req1 = new Request(recipe1, null, true);
    requests.add(req1);
    Request selected = sjfPolicy.selectRequest(requests, inventory);
    assertEquals(req1, selected);
    assertEquals("sjf", sjfPolicy.getRequestPolicyName());
  }

  /**
   * With two ready requests and different latencies, we select the one with the
   * lower latency.
   */
  @Test
  public void test_selectLowestLatency() {
    Recipe recipe1 = new Recipe("item1", new LinkedHashMap<>(), 20);
    Recipe recipe2 = new Recipe("item2", new LinkedHashMap<>(), 10);
    Request req1 = new Request(recipe1, null, true);
    Request req2 = new Request(recipe2, null, true);
    requests.add(req1);
    requests.add(req2);
    Request selected = sjfPolicy.selectRequest(requests, inventory);
    assertEquals(req2, selected);
  }

  /**
   * When two ready requests have equal latency, we select the oldest request.
   */
  @Test
  public void testSelectOldestWhenEqualLatency() {
    Recipe recipe1 = new Recipe("item1", new LinkedHashMap<>(), 15);
    Recipe recipe2 = new Recipe("item2", new LinkedHashMap<>(), 15);
    Request req1 = new Request(recipe1, null, true);
    Request req2 = new Request(recipe2, null, true);
    requests.add(req1); // Older
    requests.add(req2);
    Request selected = sjfPolicy.selectRequest(requests, inventory);
    assertEquals(req1, selected);
  }

  /**
   * Requests that are not ready because of unsatisfied ingredient requirements
   * are skipped.
   */
  @Test
  public void testSkipNotReadyRequests() {
    LinkedHashMap<String, Integer> ingredients = new LinkedHashMap<>();
    ingredients.put("wood", 3);
    Recipe recipe1 = new Recipe("item1", ingredients, 20);
    Request req1 = new Request(recipe1, null, true);
    requests.add(req1);

    Recipe recipe2 = new Recipe("item2", new LinkedHashMap<>(), 10);
    Request req2 = new Request(recipe2, null, true);
    requests.add(req2);

    inventory.put("wood", 0);

    Request selected = sjfPolicy.selectRequest(requests, inventory);
    assertEquals(req2, selected);
  }

}
