package edu.duke.ece651.simulationserver;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

public class BuildingTest {
  // Create an anonymous subclass of Building for testing
  Building building = new Building("TestBuilding", new ArrayList<>()) {
    @Override
    public boolean mayProduce(String ingredient) {
      return true;
    }

      @Override
      public String capableOf(String product) {
        return null;
      }

    @Override
    protected List<Recipe> getRecipes() {
      return Collections.emptyList();
    }
  };

  @Test
  public void test_startRequestNotInQueue() {

    Recipe dummyRecipe = new Recipe("dummy", Collections.emptyMap(), 5);
    Request notInQueue = new Request(dummyRecipe, building, false);
    assertThrows(IllegalArgumentException.class, () -> {
      building.startRequest(notInQueue);
    });
    assertTrue(building.mayProduce(null));
    assertEquals(0, building.getRecipes().size());
  }

  @Test
  public void test_getTotalLatence() {
    Recipe r1 = new Recipe("item1", Collections.emptyMap(), 5);
    Recipe r2 = new Recipe("item2", Collections.emptyMap(), 10);
    Request req1 = new Request(r1, building, false);
    Request req2 = new Request(r2, building, false);
    building.requests.add(req1);
    building.requests.add(req2);

    assertNull(building.currReq);
    int totalLat = building.getTotalLatence();
    assertEquals(15, totalLat, "Should sum latencies of queued requests if no currReq");

    building.startRequest(req1);
    building.timeLeft = 3;
    totalLat = building.getTotalLatence();
    assertEquals(13, totalLat);
  }

  @Test
  public void test_addInventory() {
    assertTrue(building.getInventory().isEmpty(), "Initially, inventory should be empty.");
    building.addInventory("wood", 5);
    assertEquals(5, building.getInventory().get("wood"));
    building.addInventory("wood", 3);
    assertEquals(8, building.getInventory().get("wood"));
    building.addInventory("metal", 2);
    assertEquals(2, building.getInventory().get("metal"));
  }

  @Test
  public void test_finishRequestInventory() {
    Map<String, Integer> inventory = new HashMap<>();
    inventory.put("wood", 5);
    inventory.put("metal", 2);
    building.setInventory(inventory);

    Map<String, Integer> ing = new HashMap<>();
    ing.put("wood", 2);
    ing.put("metal", 2);
    Recipe recipe = new Recipe("dummyOutput", ing, 10);
    Request currReq = new Request(recipe, building, false);

    building.addSource(building);
    building.setCurrReq(currReq);
    building.finishRequest();
    assertTrue(building.getInventory().containsKey("wood"));
    assertEquals(3, building.getInventory().get("wood").intValue());

    assertFalse(building.getInventory().containsKey("metal"));
  }

  @Test
  public void test_getCoordinate() {
    assertEquals(null, building.getCoordinate());
    Coordinate c1 = new Coordinate(2, 2);
    building.setCoordinate(c1);
    assertEquals(c1, building.getCoordinate());
    
  }

  @Test
  public void test_step() {
    Recipe metalRecipe = new Recipe("metal", new LinkedHashMap<String, Integer>(), 1);
    Building metalMine = new Mine("M", new ArrayList<Building>(), metalRecipe);
    ArrayList<Building> sources = new ArrayList<>();
    sources.add(metalMine);
    LinkedHashMap<String, Integer> ingredients = new LinkedHashMap<>();
    ingredients.put("metal", 1);
    Recipe hingeRecipe = new Recipe("hinge", ingredients, 1);
    ArrayList<Recipe> hingeRecipes = new ArrayList<>();
    hingeRecipes.add(hingeRecipe);
    FactoryType hingeType = new FactoryType("hinge", hingeRecipes);
    Building hingeFactory = new Factory("Hi", sources, hingeType);
    hingeFactory.addRequest(new Request(hingeRecipe, null, true));
    assertFalse(metalMine.finished());
    assertFalse(hingeFactory.finished());
    metalMine.step();
    hingeFactory.step();
    metalMine.deliver();
    hingeFactory.deliver();
    assertTrue(metalMine.finished());
    assertFalse(hingeFactory.finished());
    metalMine.step();
    hingeFactory.step();
    metalMine.deliver();
    hingeFactory.deliver();
    assertTrue(metalMine.finished());
    assertTrue(hingeFactory.finished());
  }

  @Test
  public void test_deliver() {
    Recipe metalRecipe = new Recipe("metal", new LinkedHashMap<String, Integer>(), 1);
    Building metalMine = new Mine("M", new ArrayList<Building>(), metalRecipe, new Coordinate(0, 0));
    ArrayList<Building> sources = new ArrayList<>();
    sources.add(metalMine);
    LinkedHashMap<String, Integer> ingredients = new LinkedHashMap<>();
    ingredients.put("metal", 1);
    Recipe hingeRecipe = new Recipe("hinge", ingredients, 2);
    ArrayList<Recipe> hingeRecipes = new ArrayList<>();
    hingeRecipes.add(hingeRecipe);
    FactoryType hingeType = new FactoryType("hinge", hingeRecipes);
    Building hingeFactory = new Factory("Hi", sources, hingeType, new Coordinate(2, 0));
    Connector c = new Connector();
    Map<Coordinate, Square> squares = new HashMap<>();
    squares.put(metalMine.getCoordinate(), metalMine);
    squares.put(hingeFactory.getCoordinate(), hingeFactory);
    GraphPath path = c.connect(squares, metalMine, hingeFactory, new ArrayList<>());
    assertEquals("(0, 0) -> (1, 0) -> (2, 0)", path.toString());
    assertEquals(1, path.getDistance());
    hingeFactory.addSource(metalMine, path);
    hingeFactory.addRequest(new Request(hingeRecipe, null, true));
    
    Request metalRequest = metalMine.getRequests().peek();

    hingeFactory.step();
    metalMine.step();
    assertEquals(new Coordinate(0, 0), metalMine.getRequestLocation(metalRequest));
    hingeFactory.deliver();
    metalMine.deliver();

    assertFalse(metalMine.finished());
    assertEquals(new Coordinate(1, 0), metalMine.getRequestLocation(metalRequest));    

    hingeFactory.step();
    metalMine.step();
    hingeFactory.deliver();
    metalMine.deliver();

    assertTrue(metalMine.finished());
    assertThrows(IllegalArgumentException.class, () -> metalMine.getRequestLocation(metalRequest));

    hingeFactory.step();
    metalMine.step();
    hingeFactory.deliver();
    metalMine.deliver();

    assertEquals(1, hingeFactory.getTimeLeft());
    assertFalse(hingeFactory.finished());
    
    hingeFactory.step();
    metalMine.step();
    hingeFactory.deliver();
    metalMine.deliver();
    
    assertTrue(hingeFactory.finished());
  }

  @Test
  public void test_capableOf() {
    assertNull(building.capableOf(null));
  }

}
