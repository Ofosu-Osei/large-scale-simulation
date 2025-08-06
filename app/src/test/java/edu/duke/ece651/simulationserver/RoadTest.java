package edu.duke.ece651.simulationserver;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class RoadTest {
  @Test
  public void testRoadGettersAndSetters() {
    Coordinate coord1 = new Coordinate(1, 2);
    Coordinate coord2 = new Coordinate(3, 4);
    
    Road next = new Road(coord2, "R");
    
    Road path = new Road(coord1, new int[]{0, 1});
    
    assertEquals(coord1, path.getCoordinate());
    assertArrayEquals(new int[]{0, 1}, path.getDirection());
    
    path.setCoordinate(coord2);
    assertEquals(coord2, path.getCoordinate());
    
    path.setDirection(null);
    assertArrayEquals(null, path.getDirection());
    assertThrows(IllegalArgumentException.class, () -> new Road(coord1, new int[]{1, 1}));
    assertThrows(IllegalArgumentException.class, () -> new Road(coord1, new int[]{1}));
    assertThrows(IllegalArgumentException.class, () -> new Road(coord1, new int[]{1, 1, 1}));
    assertThrows(IllegalArgumentException.class, () -> new Road(coord1, "a"));
  }
  

}
