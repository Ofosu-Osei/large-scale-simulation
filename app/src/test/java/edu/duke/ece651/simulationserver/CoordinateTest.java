package edu.duke.ece651.simulationserver;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class CoordinateTest {
    /**
     * Test the initialization of Coordinate class
     */
    @Test
    public void testCoordinateInitialization() {
        Coordinate coordinate = new Coordinate(7, 15);
        assertEquals(7, coordinate.getRow().intValue());
        assertEquals(15, coordinate.getColumn().intValue());
    }
    
    @Test
    public void testGetRow() {
        Coordinate coordinate = new Coordinate(7, 15);
        assertEquals(7, coordinate.getRow().intValue());
    }

    @Test
    public void testGetColumn() {
        Coordinate coordinate = new Coordinate(7, 15);
        assertEquals(15, coordinate.getColumn().intValue());
    }
    @Test
    public void testCoordinateWithNullValues() {
        Coordinate coordinate = new Coordinate(null, null);
        assertEquals(null, coordinate.getRow());
        assertEquals(null, coordinate.getColumn());
    }
    @Test
    public void testCoordinateWithNegativeValues() {
        Coordinate coordinate = new Coordinate(-5, -10);
        assertEquals(-5, coordinate.getRow().intValue());
        assertEquals(-10, coordinate.getColumn().intValue());
    }
    @Test
    public void testEqualsMethod() {
        Coordinate coordinate1 = new Coordinate(5, 10);
        Coordinate coordinate2 = new Coordinate(5, 10);
        Coordinate coordinate3 = new Coordinate(10, 5);
        Coordinate coordinate4 = new Coordinate(null, null);
        Coordinate coordinate5 = new Coordinate(null, null);

        assertEquals(true, coordinate1.equals(coordinate2)); // Same values
        assertEquals(false, coordinate1.equals(coordinate3)); // Different values
        assertEquals(false, coordinate1.equals("Not a Coordinate")); // Different class
        assertEquals(true, coordinate4.equals(coordinate5)); // Both null values
    }

    @Test
    public void testToStringMethod() {
        Coordinate coordinate = new Coordinate(3, 7);
        assertEquals("(3, 7)", coordinate.toString());

        Coordinate coordinateWithNull = new Coordinate(null, null);
        assertEquals("(null, null)", coordinateWithNull.toString());
    }

    @Test
    public void testHashCodeMethod() {
        Coordinate coordinate1 = new Coordinate(2, 4);
        Coordinate coordinate2 = new Coordinate(2, 4);
        Coordinate coordinate3 = new Coordinate(4, 2);

        assertEquals(coordinate1.hashCode(), coordinate2.hashCode()); // Same values
        assertEquals(false, coordinate1.hashCode() == coordinate3.hashCode()); // Different values
    }

  @Test
  public void testDistanceTo() {
    Coordinate c1 = new Coordinate(1, 2);
    Coordinate c2 = new Coordinate(4, 6);
    assertEquals(5, c1.distanceTo(c2));
    assertEquals(5, c2.distanceTo(c1));
    assertEquals(0, c1.distanceTo(c1));
  }
}


