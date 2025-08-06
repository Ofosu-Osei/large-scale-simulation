package edu.duke.ece651.simulationserver;

import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RequestTest {
  private Recipe mockRecipe;
  private Building mockRequester;
  private Request userRequest;
  private Request systemRequest;
  LinkedHashMap<String, Integer> doorIngredients = new LinkedHashMap<>();
  @BeforeEach
  public void setUp() {
    Request.resetIdGenerator();

    List<Recipe> recipes = new ArrayList<>();
    Recipe recipe = new Recipe("test_recipe", null, 10);
    recipes.add(recipe);
    
    doorIngredients.put("wood", 3);
    mockRecipe = new Recipe("door", doorIngredients, 10);
    mockRequester = new Factory(
        "Factory-1",
        new ArrayList<>(),
        new FactoryType("General", recipes));
    userRequest = new Request(mockRecipe, mockRequester, true);
    systemRequest = new Request(mockRecipe, mockRequester, false);
  }

  @Test
  public void testConstructorAndBasicGetters() {
    assertEquals(0, userRequest.getId());
    assertEquals(mockRecipe, userRequest.getRecipe());

    assertEquals("Factory-1", userRequest.getRequester().getName());
    assertEquals(RequestState.WAITING, userRequest.getState());
    assertTrue(userRequest.isUserRequest());
    assertFalse(systemRequest.isUserRequest());
  }

  @Test
  public void testIdGeneratorUniqueness() {
    Request request1 = new Request(mockRecipe, mockRequester, true);
    Request request2 = new Request(mockRecipe, mockRequester, false);
    assertEquals(2, request1.getId());
    assertEquals(3, request2.getId());
  }

  @Test
  public void testStateTransitions() {
    assertEquals(RequestState.WAITING, userRequest.getState());

    userRequest.setState(RequestState.WORKING);
    assertEquals(RequestState.WORKING, userRequest.getState());

    userRequest.finish();
    assertEquals(RequestState.READY, userRequest.getState());
  }

  @Test
  public void test_isReadyWithSufficientInventoryNoSubRequests() {
    Map<String, Integer> inventory = new HashMap<>();
    inventory.put("wood", 3);
    assertTrue(userRequest.isReady(inventory));
  }

  @Test
  public void test_isReadyFailsWithInsufficientInventory() {
    Map<String, Integer> inventory = new HashMap<>();
    inventory.put("wood", 2);
    assertFalse(userRequest.isReady(inventory));
  }

  @Test
  public void test_isReadyFailsWhenSubRequestNotCompleted() {
    Recipe metalRecipe = new Recipe("metal", new LinkedHashMap<>(), 1);
    Request subReq = new Request(metalRecipe, mockRequester, false);   
    userRequest.addSubRequest(subReq);
    Map<String, Integer> inventory = new HashMap<>();
    inventory.put("metal", 2);
    assertFalse(userRequest.isReady(inventory));
    
  }

  @Test
  public void test_isReadySucceedsWhenSubRequestCompleted() {
    doorIngredients.put("metal", 2);
    Map<String, Integer> inventory = new HashMap<>();
    inventory.put("wood", 3);
    Recipe metalRecipe = new Recipe("metal", new LinkedHashMap<>(), 1);
    Request subReq = new Request(metalRecipe, mockRequester, false);
    subReq.finish();
    inventory.put("metal", 2);
    
    assertTrue(userRequest.isReady(inventory));
  }
}
