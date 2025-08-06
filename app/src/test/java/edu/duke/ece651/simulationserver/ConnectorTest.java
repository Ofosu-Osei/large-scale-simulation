package edu.duke.ece651.simulationserver;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ConnectorTest {
  Connector c = new Connector();
  Map<Coordinate, Square> squares;
  List<Road> roads;
  
  @BeforeEach
  public void init() {
    squares = new HashMap<>();
    roads = new ArrayList<>();
  }

  @Test
  public void test_connect_no_path() {
    GraphPath ans = c.connect(squares, new Factory(new Coordinate(0, 0)), new Factory(new Coordinate(0, 2)), roads);
    assertEquals("(0, 0) -> (0, 1) -> (0, 2)", ans.toString());
    assertEquals(2, ans.getCost());
    Square s = squares.get(new Coordinate(0, 1));
    assertEquals(Road.class, s.getClass());
    Road r = (Road) s;
    assertEquals(new Coordinate(0, 1), r.getCoordinate());
    int[] dir = r.getDirection();
    assertEquals(0, dir[0]);
    assertEquals(1, dir[1]);
  }

  @Test
  public void test_no_connection() {
    Recipe r = new Recipe("product", new HashMap<>(), 1);
    Storage s = new Storage("s", r, new ArrayList<>(), 10, 1.0);
    squares.put(new Coordinate(0, 1), s);
    squares.put(new Coordinate(0, -1), s);
    squares.put(new Coordinate(1, 0), s);
    squares.put(new Coordinate(-1, 0), s);
    assertThrows(IllegalArgumentException.class, () -> c.connect(squares, new Factory(new Coordinate(0, 0)), new Factory(new Coordinate(1, 1)), roads));
  }

  @Test
  public void test_min_cost() {
    Coordinate pathCoord1 = new Coordinate(1, 0);
    Road r1 = new Road(pathCoord1, "r");
    squares.put(pathCoord1, r1);
    roads.add(r1);
    
    Coordinate pathCoord2 = new Coordinate(1, 1);
    Road r2 = new Road(pathCoord2);
    squares.put(pathCoord2, r2);
    roads.add(r2);
    
    Coordinate pathCoord3 = new Coordinate(1, 2);
    Road r3 = new Road(pathCoord3, "r");
    squares.put(pathCoord3, r3);
    roads.add(r3);
    
    Coordinate pathCoord4 = new Coordinate(1, -1);
    Road r4 = new Road(pathCoord4, "r");
    squares.put(pathCoord4, r4);
    roads.add(r4);
    
    GraphPath ans1 = c.connect(squares, new Factory(new Coordinate(0, 0)), new Factory(new Coordinate(1, 3)), roads);
    assertEquals("(0, 0) -> (1, 0) -> (1, 1) -> (1, 2) -> (1, 3)", ans1.toString());
    assertEquals(3, ans1.getCost());
    GraphPath ans2 = c.connect(squares, new Factory(new Coordinate(0, 0)), new Factory(new Coordinate(1, -2)), roads);
    assertEquals("(0, 0) -> (0, -1) -> (1, -1) -> (1, -2)", ans2.toString());
    assertEquals(3, ans2.getCost());
  }
}
