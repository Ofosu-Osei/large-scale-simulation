package edu.duke.ece651.simulationserver;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

public class GraphPathTest {
  @Test
  public void test_constructors() {
    GraphPath gp1 = new GraphPath();
    ArrayList<Coordinate> coords = new ArrayList<>();
    coords.add(new Coordinate(0, 0));
    coords.add(new Coordinate(1, 1));
    GraphPath gp2 = new GraphPath(coords, 3);
    assertEquals("", gp1.toString());
    assertEquals(0, gp1.getDistance());
    assertEquals("(0, 0) -> (1, 1)", gp2.toString());
    assertEquals(-3, gp1.compareTo(gp2));
    assertEquals(0, gp2.getDistance());
  }

  @Test
  public void test_addNode() {
    GraphPath gp1 = new GraphPath();
    gp1.addNode(new Coordinate(1, 1), 0);
    GraphPath gp2 = gp1.copyAndAddNode(new Coordinate(2, 2), 0);
    gp2.addNode(new Coordinate(3, 3), 0);
    assertEquals("(1, 1)", gp1.toString());
    assertEquals("(1, 1) -> (2, 2) -> (3, 3)", gp2.toString());
    assertEquals(0, gp1.getDistance());
    assertEquals(1, gp2.getDistance());
  }

  @Test
  public void test_getters() {
    GraphPath gp1 = new GraphPath();
    ArrayList<Coordinate> coords = new ArrayList<>();
    coords.add(new Coordinate(0, 0));
    coords.add(new Coordinate(1, 1));
    GraphPath gp2 = new GraphPath(coords, 3);
    assertEquals(0, gp1.getCost());
    assertEquals(3, gp2.getCost());
    assertNull(gp1.getEnd());
    assertEquals(new Coordinate(1, 1), gp2.getEnd());
  }

  @Test
  public void test_turnNum() {
    ArrayList<Coordinate> coords1 = new ArrayList<>();
    coords1.add(new Coordinate(0, 0));
    coords1.add(new Coordinate(0, 1));
    coords1.add(new Coordinate(1, 1));
    coords1.add(new Coordinate(1, 2));
    coords1.add(new Coordinate(2, 2));
    GraphPath gp1 = new GraphPath(coords1, 0);
    assertEquals(3, gp1.getTurnNum());
    ArrayList<Coordinate> coords2 = new ArrayList<>();
    coords2.add(new Coordinate(0, 0));
    coords2.add(new Coordinate(0, 1));
    coords2.add(new Coordinate(0, 2));
    coords2.add(new Coordinate(0, 3));
    coords2.add(new Coordinate(0, 4));
    GraphPath gp2 = new GraphPath(coords2, 0);
    assertEquals(0, gp2.getTurnNum());
  }
}
