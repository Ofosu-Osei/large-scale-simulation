
package edu.duke.ece651.simulationserver;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

public class SimulationTest {
  @Test
  public void test_readJson() throws IOException {
    Simulation simulation = new Simulation("src/test/resources/doors1.json");
    Map<String, Recipe> recipes = simulation.getRecipes();
    assertEquals(recipes.size(), 5);
    Recipe r = recipes.get("door");
    assertEquals(r.getOutput(), "door");
    assertEquals(r.getLatency(), 12);
    Map<String, FactoryType> types = simulation.getTypes();
    assertEquals(types.size(), 3);
    Map<String, Building> buildings = simulation.getBuildings();
    assertEquals(buildings.size(), 5);
    Building b0 = simulation.getBuilding("D");
    assertEquals(b0.getName(), "D");
    assertEquals(b0.getClass(), Factory.class);
    List<Building> sources_b0 = b0.getSources();
    boolean hasAllThree = sources_b0.stream()
    .map(Building::getName)
    .collect(Collectors.toSet())
    .containsAll(Arrays.asList("W", "Hi", "Ha"));
    assertTrue(hasAllThree);

    Building b1 = simulation.getBuilding("Ha");
    assertTrue(b1.mayProduce("handle"));
    assertTrue(b1 instanceof Factory);
    List<Recipe> recipes_b1 = b1.getRecipes();
    
    for (Recipe r_ : recipes_b1) {
      assertEquals(r_.getOutput(), "handle");
    }
    
    Recipe handle = recipes.get("handle");
    assertEquals(handle.getOutput(), "handle");
    assertEquals(handle.getLatency(), 5);
    
  }

  @Test
  public void test_step() throws IOException {
    Simulation simulation = new Simulation("src/test/resources/doors1.json");
    assertThrows(IllegalArgumentException.class, () -> simulation.stepN(-1));
    int cycle = Simulation.getCycle();
    simulation.stepN(2);
    assertEquals(Simulation.getCycle(), cycle + 2);
  }

  @Test
  public void test_setPolicy() throws IOException {
    Simulation simulation = new Simulation("src/test/resources/doors1.json");
    assertThrows(IllegalArgumentException.class, () -> simulation.setRequestPolicy("d", "ready"));
    assertThrows(IllegalArgumentException.class, () -> simulation.setSourcePolicy("d", "qlen"));
    assertThrows(IllegalArgumentException.class, () -> simulation.setRequestPolicy("D", "redy"));
    assertThrows(IllegalArgumentException.class, () -> simulation.setSourcePolicy("D", "Qlen"));
    simulation.setRequestPolicy("D", "ready");
    simulation.setSourcePolicy("D", "qlen");
    Building b0 = simulation.getBuilding("D");
    Building b1 = simulation.getBuilding("Hi");
    assertEquals(b0.getRequestPolicy().getClass(), ReadyPolicy.class);
    assertEquals(b0.getSourcePolicy().getClass(), QlenPolicy.class);

    assertThrows(IllegalArgumentException.class, () -> simulation.setRequestDefault("redy"));
    assertThrows(IllegalArgumentException.class, () -> simulation.setSourceDefault("Qlen"));
    simulation.setRequestDefault("sjf");
    simulation.setSourceDefault("qlen");
    assertEquals(b0.getRequestPolicy().getClass(), ReadyPolicy.class);
    assertEquals(b1.getRequestPolicy().getClass(), SjfPolicy.class);
    assertEquals(b0.getSourcePolicy().getClass(), QlenPolicy.class);
    
    assertThrows(IllegalArgumentException.class, () -> simulation.setRequestAll("redy"));
    assertThrows(IllegalArgumentException.class, () -> simulation.setSourceAll("Qlen"));
    simulation.setRequestAll("fifo");
    simulation.setSourceAll("qlen");
    assertEquals(b0.getRequestPolicy().getClass(), FifoPolicy.class);
    assertEquals(b0.getRequestPolicy().getClass(), FifoPolicy.class);
    assertEquals(b0.getSourcePolicy().getClass(), QlenPolicy.class);
  }

  @Test
  public void test_request() throws IOException {
    Simulation simulation = new Simulation("src/test/resources/doors1.json");
    // simulation.request("D", "door");
    assertThrows(IllegalArgumentException.class, () -> simulation.request("d", "door"));
    assertThrows(IllegalArgumentException.class, () -> simulation.request("D", "Door"));
    simulation.request("Ha", "handle");
    // Building handleFactory = simulation.getBuilding("Ha");
    // assertFalse(handleFactory.finished());
    // System.out.println(simulation.getCycle());
    // simulation.stepN(3);
    // System.out.println(simulation.getCycle());
    // assertFalse(handleFactory.finished());
    // simulation.stepN(5);
    // System.out.println(simulation.getCycle());
    // assertTrue(handleFactory.finished());
    // System.out.println("test end");
  }

  @Test
  public void test_disconnect() throws IOException {
    Simulation simulation = new Simulation("src/test/resources/doors1.json");
    simulation.disconnect("Ha", "D");
    Map<Coordinate, Square> squares = simulation.getSquares();
    // for (Map.Entry<Coordinate, Square> entry : squares.entrySet()) {
    //   System.out.println(entry.getKey() + ": " + entry.getValue());
    // }
    assertFalse(squares.containsKey(new Coordinate(1, 0)));
    assertThrows(IllegalArgumentException.class, () -> simulation.request("D", "door"));
  }

  @Test
  public void test_createDronePort() throws IOException {
    Simulation simulation = new Simulation("src/test/resources/doors1.json");
    simulation.createBuilding("src/test/resources/newDronePort.json");
    Building b = simulation.getBuilding("DP");
    assertNotNull(b);
    assertEquals("DP", b.getName());
  }

  @Test
  public void test_save_load_drone() throws IOException {
    Simulation simulation = new Simulation("src/test/resources/doors1.json");
    simulation.createBuilding("src/test/resources/newDronePort.json");
    simulation.addDrone("DP");
    simulation.request("Hi", "hinge");
    simulation.stepN(4);
    simulation.save("save_with_drone.json");
    simulation.finish();
    assertEquals(1, Simulation.getDronePorts().size());
    simulation = new Simulation("save_with_drone.json");
    assertEquals(4, Simulation.getCycle());
  }
}
