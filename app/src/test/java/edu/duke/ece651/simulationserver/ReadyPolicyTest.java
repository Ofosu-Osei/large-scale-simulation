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

public class ReadyPolicyTest {
  private ReadyPolicy readyPolicy;
  private Queue<Request> requests;
  private Map<String, Integer> inventory;

  @BeforeEach
  public void setUp() {
    readyPolicy = new ReadyPolicy();
    requests = new LinkedList<>();
    inventory = new HashMap<>();
  }

  /**
   * When the queue is empty, selectRequest should returns null.
   */
  @Test
  public void test_emptyQueue() {
    Request selected = readyPolicy.selectRequest(requests, inventory);
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
    Request selected = readyPolicy.selectRequest(requests, inventory);
    assertEquals(req1, selected);
  }

  /**
   * With a not ready request in the front and a ready request, 
   * we select the one which is ready
   */
  @Test
  public void test_skipNotReadyRequests() {
    LinkedHashMap<String, Integer> ingredients = new LinkedHashMap<>();
    ingredients.put("wood", 3);
    Recipe recipe1 = new Recipe("item1", ingredients, 20);
    Request req1 = new Request(recipe1, null, true);
    requests.add(req1);

    Recipe recipe2 = new Recipe("item2", new LinkedHashMap<>(), 10);
    Request req2 = new Request(recipe2, null, true);
    requests.add(req2);

    inventory.put("wood", 0);
    Request selected = readyPolicy.selectRequest(requests, inventory);
    assertEquals(req2, selected);
  }

  /**
   * With two ready requests, we select the one in the front
   */
  @Test
  public void test_selectOldestWhenAllReady() {
    Recipe recipe1 = new Recipe("item1", new LinkedHashMap<>(), 15);
    Recipe recipe2 = new Recipe("item2", new LinkedHashMap<>(), 15);
    Request req1 = new Request(recipe1, null, true);
    Request req2 = new Request(recipe2, null, true);
    requests.add(req1); // Older
    requests.add(req2);
    Request selected = readyPolicy.selectRequest(requests, inventory);
    assertEquals(req1, selected);
  }

  /**
   * Requests that are not ready because of unsatisfied ingredient requirements
   * are skipped.
   */
  @Test
  public void test_skipAllNotReadyRequests() {
    LinkedHashMap<String, Integer> ingredients1 = new LinkedHashMap<>();
    ingredients1.put("wood", 3);
    Recipe recipe1 = new Recipe("item1", ingredients1, 20);
    Request req1 = new Request(recipe1, null, true);
    requests.add(req1);

    LinkedHashMap<String, Integer> ingredients2 = new LinkedHashMap<>();
    ingredients2.put("wood", 5);
    Recipe recipe2 = new Recipe("item2", ingredients2, 10);
    Request req2 = new Request(recipe2, null, true);
    requests.add(req2);

    inventory.put("wood", 2);
    Request selected = readyPolicy.selectRequest(requests, inventory);
    assertEquals(null, selected);
  }

}
