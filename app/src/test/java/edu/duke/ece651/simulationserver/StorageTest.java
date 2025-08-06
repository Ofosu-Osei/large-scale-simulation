package edu.duke.ece651.simulationserver;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.jupiter.api.Test;

public class StorageTest {
  
  
  @Test
  public void test_constructor() {
    ArrayList<Building> sources = new ArrayList<>();
    Recipe woodRecipe = new Recipe("wood", new HashMap<String, Integer>(), 1);
    Building woodMine = new Mine("W", new ArrayList<Building>(), woodRecipe);
    sources.add(woodMine);
    Storage s = new Storage("wood storage", woodRecipe, sources, 100, 1.7);
    assertEquals(0, s.getFreq());
    assertEquals(100, s.getRemain());
    assertFalse(s.mayProduce("metal"));
    assertTrue(s.mayProduce("wood"));
  }

  @Test
  public void test_step() {
    ArrayList<Building> sources = new ArrayList<>();
    Recipe woodRecipe = new Recipe("wood", new HashMap<String, Integer>(), 1);
    Building woodMine = new Mine("W", new ArrayList<Building>(), woodRecipe);
    sources.add(woodMine);
    Storage s = new Storage("wood storage", woodRecipe, sources, 100, 1.7);
    s.step();
    s.deliver();
    assertEquals(0, s.getFreq());
    assertEquals(99, s.getRemain());
    assertThrows(IllegalArgumentException.class, () -> s.addIngredient("metal"));
    s.addIngredient("wood");
    assertEquals(1, s.getAmount());
    s.step();
    s.deliver();    
    assertEquals(1, s.getFreq());
    assertEquals(98, s.getRemain());
    assertEquals(-1, s.getQlen());
    assertEquals(-1, s.getSimplelat());
    Request request = new Request(woodRecipe, null, true);
    s.addRequest(request);
    assertEquals(99, s.getRemain());
    
    s.step();
    assertEquals(98, s.getRemain());
    s.deliver();
    assertEquals(98, s.getRemain());
    assertTrue(s.finished());
    assertEquals(0, s.getAmount());
  }

  @Test
  public void test_getters() {
    ArrayList<Building> sources = new ArrayList<>();
    Recipe woodRecipe = new Recipe("wood", new HashMap<String, Integer>(), 1);
    Building woodMine = new Mine("W", new ArrayList<Building>(), woodRecipe);
    sources.add(woodMine);
    Storage s = new Storage("wood storage", woodRecipe, sources, 100, 1.7);
    assertEquals(100, s.getCapacity());
    assertEquals(1.7, s.getPriority());
    assertEquals(woodRecipe, s.getRecipe());
  }
}
