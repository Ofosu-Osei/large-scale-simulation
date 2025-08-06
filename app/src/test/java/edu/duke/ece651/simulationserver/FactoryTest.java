package edu.duke.ece651.simulationserver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FactoryTest {

  private FactoryType type;
  private Recipe recipe;
  private String ingredient;

  @BeforeEach
  public void setUp() {
    ingredient = "test_ingredient";
    recipe = new Recipe(ingredient, null, 10);
    List<Recipe> recipes = new ArrayList<>();
    recipes.add(recipe);
    type = new FactoryType("TestType", recipes);
  }

  /*
   * @Test
   * public void test_checkIngredients() {
   * Factory testFactory = new Factory("TestFactory", new ArrayList<>(),
   * testType);
   * Map<String, Integer> required = new HashMap<>();
   * assertTrue(testFactory.checkIngredients(null));
   * assertTrue(testFactory.checkIngredients(required));
   * required.put("A", 3);
   * assertFalse(testFactory.checkIngredients(required));
   * testFactory.addInventory("A", 2);
   * assertFalse(testFactory.checkIngredients(required));
   * testFactory.addInventory("A", 1);
   * assertTrue(testFactory.checkIngredients(required));
   * 
   * }
   */

  @Test
  public void test_canProduce() {
    Factory f1 = new Factory("f1", null, type);
    assertTrue(f1.mayProduce(ingredient));
    assertFalse(f1.mayProduce("False ingredient"));
  }

  @Test
  public void test_allocateSubRequest() {
    List<Building> sources = new ArrayList<>();
    Factory f1 = new Factory("f1", null, type);
    sources.add(f1);
    assertEquals(0, f1.getHowManyRequests());
    String father_ingredient = "father_ingredient";
    Map<String, Integer> ingredients = new LinkedHashMap<>();
    ingredients.put(ingredient, 2);
    Recipe r2 = new Recipe(father_ingredient, ingredients, 20);
    List<Recipe> recipes_2 = new ArrayList<>();
    recipes_2.add(r2);
    FactoryType type2 = new FactoryType("f2 type", recipes_2);
    Factory f2 = new Factory("f2", sources, type2);
    Request request = new Request(r2, null, true);
    f2.allocateSubRequest(request);
    assertEquals(2, f1.getHowManyRequests());
    Queue<Request> requests_2 = f1.getRequests();
    assertEquals(requests_2.peek().getRequester(), f2);
    assertEquals(requests_2.peek().getRecipe(), recipe);
    assertEquals(requests_2.peek().isUserRequest(), false);
  }

  @Test
  public void test_findRecipe() {
    Recipe r1 = new Recipe("r1", null, 10);

    List<Recipe> recipes = new ArrayList<>();
    recipes.add(r1);

    Recipe result1 = Factory.findRecipe(recipes, "r1");
    assertNotNull(result1);
    assertEquals(r1, result1);

    Recipe result2 = Factory.findRecipe(recipes, "r3");
    assertNull(result2);

  }

  @Test
  public void test_allocateSubRequestNoSourceFound() {
    Map<String, Integer> ing = new HashMap<>();
    ing.put("wood", 1);
    Recipe doorRecipe = new Recipe("door", ing, 12);

    List<Recipe> recipeList = new ArrayList<>();
    recipeList.add(doorRecipe);
    FactoryType doorType = new FactoryType("doorType", recipeList);
    Factory factory = new Factory("F1", new ArrayList<>(), doorType);
    factory.setSourcePolicy(new QlenPolicy());
    Request req = new Request(doorRecipe, factory, false);
    assertThrows(IllegalArgumentException.class, () -> {
      factory.addRequest(req);
    });
  }

}
