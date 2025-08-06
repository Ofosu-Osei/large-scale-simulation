package edu.duke.ece651.simulationserver;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class NoCollisionRuleCheckerTest {
  private FactoryType type;
  private Recipe recipe;
  private String ingredient;
  private Mine woodMine;
  private Recipe woodRecipe;
  private List<Building> emptySources;

  @BeforeEach
  public void setUp() {
    ingredient = "test_ingredient";
    recipe = new Recipe(ingredient, null, 10);
    List<Recipe> recipes = new ArrayList<>();
    recipes.add(recipe);
    type = new FactoryType("TestType", recipes);
    LinkedHashMap<String, Integer> woodIng = new LinkedHashMap<>();
    woodRecipe = new Recipe("wood", woodIng, 2);
    // Mines usually have no sources, we pass an empty list
    emptySources = new ArrayList<>();
    woodMine = new Mine("WoodMine", emptySources, woodRecipe);

  }

  @Test
  public void test_NoCollisionRuleCheckerTest() {
    PlacementRuleChecker checker = new NoCollisionRuleChecker(null);
    Map<Coordinate, Square> buildings = new LinkedHashMap<>();
    Coordinate c1 = new Coordinate(1, 1);
    Coordinate c2 = new Coordinate(2, 2);
    Building b1 = new Factory("f1", null, type);
    Building b2 = new Factory("f2", null, type, c1);
    Building b3 = new Mine("WoodMine", emptySources, woodRecipe, c2);
    buildings.put(new Coordinate(0, 0), b1);
    buildings.put(c1, b2);
    buildings.put(c2, b3);
    Coordinate c4 = new Coordinate(4, 4);
    assertEquals("That placement is invalid: the building overlaps another building.", checker.checkPlacement(new Factory("f3", null, type, c1), buildings));
    assertEquals(null, checker.checkPlacement(new Factory("f4", null, type, c4), buildings));
  }

}
