package edu.duke.ece651.simulationserver;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.jupiter.api.Test;

public class DroneTest {
  @Test
  public void test_() {
    Drone drone = new Drone(new Coordinate(0, 0));
    
    Recipe metalRecipe = new Recipe("metal", new HashMap<String, Integer>(), 1);
    Building metalMine = new Mine("metal mine", new ArrayList<Building>(), metalRecipe, new Coordinate(6, 0));
    Building hingeFactory = new Factory(null, null, null, new Coordinate(0, 4));

    Request request = new Request(metalRecipe, hingeFactory, false);

    drone.requestDelivery(metalMine.getCoordinate(), request);
    drone.fly();
    assertEquals(drone.getRow(), 5);
    assertEquals(drone.getColumn(), 0);
    drone.fly();
    assertEquals(drone.getRow(), 6);
    assertEquals(drone.getColumn(), 0);
    drone.fly();
    assertTrue(drone.isInUse());
    drone.fly();
    assertEquals(drone.getRow(), 0);
    assertEquals(drone.getColumn(), 4);
    drone.fly();
    assertEquals(drone.getRow(), 0);
    assertEquals(drone.getColumn(), 0);
    assertFalse(drone.isInUse());
    // System.out.println("row: " + drone.getRow() + ", column: " + drone.getColumn());
  }
}
