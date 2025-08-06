package edu.duke.ece651.simulationserver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class QlenPolicyTest {
  private QlenPolicy qlenPolicy;
  private Map<Building, GraphPath> sources;
  private String ingredient;
  private FactoryType type;
  private Recipe recipe;
  private Map<String, Integer> chooseStandard;
    
  @BeforeEach
  public void setUp() {
    qlenPolicy = new QlenPolicy();
    sources = new LinkedHashMap<>();
    ingredient = "test_ingredient";
    recipe = new Recipe(ingredient, null, 10);
    List<Recipe> recipes = new ArrayList<>();
    recipes.add(recipe);
    type = new FactoryType("TestType", recipes);
    chooseStandard = new HashMap<>();
  }

  
  @Test
  public void test_emptySource() {
    Building selected = qlenPolicy.selectSource(sources, ingredient, chooseStandard);
    assertNull(selected);
  }
  
  @Test
  public void test_twoBuildings() {
    Factory f1 = new Factory("f1", new ArrayList<>(), type);
    Factory f2 = new Factory("f2", new ArrayList<>(), type);
    Factory f3 = new Factory("f3", new ArrayList<>(), type);
    sources.put(f1, new GraphPath());
    sources.put(f2, new GraphPath());
    sources.put(f3, null);
    Building selected1 = qlenPolicy.selectSource(sources, ingredient, chooseStandard);
    System.out.println(selected1.getName());
    assertEquals(f1, selected1);
    f1.addRequest(new Request(recipe, null, true));
    Building selected2 = qlenPolicy.selectSource(sources, ingredient, chooseStandard);
    assertEquals(f2, selected2);
  }

}
