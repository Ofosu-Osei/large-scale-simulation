package edu.duke.ece651.simulationserver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class FifoPolicyTest {
  private FifoPolicy fifoPolicy;
  private Queue<Request> requests;
  private Map<String, Integer> inventory;

  @BeforeEach
  public void setUp() {
    fifoPolicy = new FifoPolicy();
    requests = new LinkedList<>();
    inventory = new HashMap<>();
  }

  
  @Test
  public void test_emptyQueue() {
    Request selected = fifoPolicy.selectRequest(requests, inventory);
    assertNull(selected);
  }

  @Test
  public void test_singleReadyRequest() {
    Recipe recipe1 = new Recipe("item1", new LinkedHashMap<>(), 10);
    Request req1 = new Request(recipe1, null, true);
    requests.add(req1);
    Request selected = fifoPolicy.selectRequest(requests, inventory);
    assertEquals(req1, selected);
  }

  @Test
  public void test_twoRequests() {
    LinkedHashMap<String, Integer> ingredients = new LinkedHashMap<>();
    ingredients.put("wood", 3);
    Recipe recipe1 = new Recipe("item1", ingredients, 20);
    Request req1 = new Request(recipe1, null, true);
    requests.add(req1);
    Request selected1 = fifoPolicy.selectRequest(requests, inventory);
    //    assertNull(selected1);
    assertEquals(selected1, req1);

    Recipe recipe2 = new Recipe("item2", new LinkedHashMap<>(), 20);
    Request req2 = new Request(recipe2, null, true);
    requests.add(req2);

    Request selected2 = fifoPolicy.selectRequest(requests, inventory);
    //    assertNull(selected2);
    assertEquals(selected2, req1);

    inventory.put("wood", 4);
    Request selected3 = fifoPolicy.selectRequest(requests, inventory);
    assertEquals(selected3, req1);

    
  }
}
