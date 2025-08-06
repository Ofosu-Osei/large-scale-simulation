package edu.duke.ece651.simulationserver;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class DronePortTest {
  @Test
  public void test_useDrone() {
    DronePort port = new DronePort("port", new Coordinate(0, 0));
    Building b1 = new Factory(null, null, null, new Coordinate(10, 0));
    Building b2 = new Factory(null, null, null, new Coordinate(0, 10));
    Building b3 = new Factory(null, null, null, new Coordinate(21, 0));
    Recipe metalRecipe = new Recipe("metal", null, 1);
    Request r = new Request(metalRecipe, b2, false);
    // Request r2 = new Request(metalRecipe, b2, false);
    assertFalse(port.useDrone(b3, r));
    assertFalse(port.useDrone(b1, r));
    port.addDrone();
    assertTrue(port.useDrone(b1, r));
    assertFalse(port.finished());
    for (int i = 0; i < 3; i++) {
      port.deliver();
    }
    assertEquals(1, port.getRequests().size());
    for (int i = 0; i < 3; i++) {
      port.deliver();
    }
    assertFalse(port.useDrone(b1, r));
    assertFalse(port.finished());
    port.deliver();
    assertEquals(0, port.getRequests().size());
    // System.out.println(port.getRequests().size());
    assertTrue(port.finished());
    assertTrue(port.useDrone(b1, r));
  }
}
