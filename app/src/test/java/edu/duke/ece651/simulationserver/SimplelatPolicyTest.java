package edu.duke.ece651.simulationserver;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class SimplelatPolicyTest {
  private SimplelatPolicy policy;
  private Map<Building, GraphPath> sources;
  private String ingredient;
  private FactoryType type;
  private Map<String, Integer> chooseStandard;
  private GraphPath gp;
  @BeforeEach
  public void setUp() {
    policy = new SimplelatPolicy();
    sources = new LinkedHashMap<>();
    ingredient = "test_ingredient";
    //type = new FactoryType("test_ingredient");
    chooseStandard = new HashMap<>();
    gp = new GraphPath();
  }

  @Test
  public void test_emptySource() {
    Building selected = policy.selectSource(sources, ingredient, chooseStandard);
    assertNull(selected);
  }

  @Test
  public void test_noValidBuilding() {
    List<Recipe> l1 = new ArrayList<>();
    l1.add(new Recipe("TestIngredient", null, 10));
    Factory invalidFactory = new Factory("f1", new ArrayList<>(), new FactoryType("TestIngredient", l1));
    sources.put(invalidFactory, null);
    
    Building selected = policy.selectSource(sources, ingredient, chooseStandard);
    assertNull(selected);
  }

  @Test
  public void test_singleValidBuilding() {
    LinkedHashMap<String, Integer> ingredients = new LinkedHashMap<>();
    Recipe recipe = new Recipe("test_ingredient", ingredients, 10);
    List<Recipe> l1 = new ArrayList<>();
    l1.add(recipe);
    FactoryType type = new FactoryType("test_ingredient", l1);
    
    Factory validFactory = new Factory("f1", new ArrayList<>(), type);
    sources.put(validFactory, gp);
    validFactory.addRequest(new Request(recipe, null, true));
    Building selected = policy.selectSource(sources, ingredient, chooseStandard);
    assertEquals(validFactory, selected);
    assertEquals("simpleLat", policy.getSourcePolicyName());
  }

  @Disabled
  @Test
  public void test_twoBuildings() {

    List<Recipe> l1 = new ArrayList<>();
    List<Recipe> l2 = new ArrayList<>();
    LinkedHashMap<String, Integer> ingredients = new LinkedHashMap<>();
    ingredients.put("wood", 2);
    ingredients.put("steel", 3);
    Recipe recipe1 = new Recipe("test_ingredient", ingredients, 10);
    Recipe recipe2 = new Recipe("test_ingredient", ingredients, 10);
    
    l1.add(recipe1);
    l2.add(recipe2);

    FactoryType type = new FactoryType("test_ingredient", l1);
    FactoryType type2 = new FactoryType("test_ingredient", l2);
    Factory f1 = new Factory("f1", new ArrayList<>(), type);
    Factory f2 = new Factory("f2", new ArrayList<>(), type2);
    sources.put(f1, gp);
    sources.put(f2, gp);
    f1.onlyAddRequest(new Request(recipe1, null, true));
    f2.onlyAddRequest(new Request(recipe2, null, true));
    Building selected2 = policy.selectSource(sources, ingredient, chooseStandard);
    assertEquals(f1, selected2);
  }
}
