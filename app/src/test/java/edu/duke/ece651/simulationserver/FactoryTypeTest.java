package edu.duke.ece651.simulationserver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.junit.jupiter.api.Test;

public class FactoryTypeTest {
  @Test
  public void test_getters() {
    Recipe recipe = new Recipe("item1", new LinkedHashMap<>(), 10);
    List<Recipe> recipes = new ArrayList<>();
    recipes.add(recipe);
   
    FactoryType type = new FactoryType("test", recipes);
    assertEquals("test", type.getName());
    assertEquals(recipes, type.getRecipes());
    assertThrows(IllegalArgumentException.class, () -> new FactoryType("null_recipes", null));
  }
}
