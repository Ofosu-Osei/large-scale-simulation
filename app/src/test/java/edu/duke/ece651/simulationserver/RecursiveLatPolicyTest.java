package edu.duke.ece651.simulationserver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RecursiveLatPolicyTest {

  private RecursiveLatPolicy policy;
  private Map<String, Recipe> recipeMap;
  private Map<String, Integer> chooseStandard;
  private GraphPath gp;
  
  private static class TestBuilding extends Building {
    private boolean canProduceFlag;

    public TestBuilding(String name, boolean canProduceFlag) {
      super(name, new ArrayList<>());
      this.canProduceFlag = canProduceFlag;

    }

    @Override
    protected List<Recipe> getRecipes() {
      return Collections.emptyList();
    }

    @Override
    public boolean mayProduce(String ingredient) {
      return canProduceFlag;
    }

    @Override
    public String capableOf(String product) {
      if (mayProduce(product)) {
        return null;
      }
      return "error";
    }
  }

  // stb constructor for a Recipe
  private static class TestRecipe extends Recipe {
    public TestRecipe(String output, LinkedHashMap<String, Integer> ingredients, int latency) {
      super(output, ingredients, latency);
    }
  }

  // A simple subclass of Storage for testing purposes.
  private static class TestStorage extends Storage {
    public TestStorage(String name, Recipe storesRecipe, List<Building> sourcesList, int cap, double pri) {
      // This uses the constructor that sets coordinate to (0,0) by default.
      super(name, storesRecipe, sourcesList, cap, pri);
    }
  }

  @BeforeEach
  public void setUp() {
    // Create a recipeMap with some sample recipes
    recipeMap = new HashMap<>();
    recipeMap.put("wood", new TestRecipe("wood", new LinkedHashMap<>(), 2));
    recipeMap.put("metal", new TestRecipe("metal", new LinkedHashMap<>(), 3));
    LinkedHashMap<String, Integer> hingeIng = new LinkedHashMap<>();
    hingeIng.put("metal", 1);
    recipeMap.put("hinge", new TestRecipe("hinge", hingeIng, 4));
    LinkedHashMap<String, Integer> doorIng = new LinkedHashMap<>();
    doorIng.put("wood", 1);
    doorIng.put("hinge", 2);
    recipeMap.put("door", new TestRecipe("door", doorIng, 10));

    policy = new RecursiveLatPolicy(recipeMap);

    chooseStandard = new HashMap<>();
    gp = new GraphPath();
  }

  @Test
  public void test_selectSource_noCandidates() {
    // If sourceList is empty, expect null
    Map<Building, GraphPath> sources = new LinkedHashMap<>();
    Building result = policy.selectSource(sources, "wood", chooseStandard);
    assertNull(result);
  }

  @Test
  public void test_selectSource_noOneCanProduce() {
    // Two buildings, but canProduceFlag is false
    Map<Building, GraphPath> sources = new LinkedHashMap<>();
    sources.put(new TestBuilding("B1", false), gp);
    sources.put(new TestBuilding("B2", false), gp);

    // None can produce wood
    Building result = policy.selectSource(sources, "wood", chooseStandard);
    assertNull(result);
  }

  @Test
  public void test_selectSource_singleCandidate_noRequests() {
    // One building that can produce
    TestBuilding b1 = new TestBuilding("B1", true);
    Map<Building, GraphPath> sources = new LinkedHashMap<>();
    sources.put(b1, gp);
    
    Building result = policy.selectSource(sources, "wood", chooseStandard);
    assertEquals(b1, result);
  }

  @Test
  public void test_selectSource_twoCandidates_compareQueueEstimates() {
    // Building B1 can produce, has 1 request
    TestBuilding b1 = new TestBuilding("B1", true);
    // Building B2 can produce, has 2 requests
    TestBuilding b2 = new TestBuilding("B2", true);

    Request r1 = new Request(recipeMap.get("wood"), b1, false);
    b1.requests.add(r1);

    Request r2 = new Request(recipeMap.get("metal"), b2, false);
    Request r3 = new Request(recipeMap.get("door"), b2, false);
    b2.requests.add(r2);
    b2.requests.add(r3);

    Map<Building, GraphPath> sources = new LinkedHashMap<>();
    sources.put(b1, gp);
    sources.put(b2, gp);
    
    Building result = policy.selectSource(sources, "hinge", chooseStandard);
    assertEquals(b1, result);
  }

  @Test
  public void test_selectSource_tieBreakByOrder() {
    TestBuilding b1 = new TestBuilding("B1", true);
    TestBuilding b2 = new TestBuilding("B2", true);

    Request r1 = new Request(recipeMap.get("wood"), b1, false);
    b1.requests.add(r1);

    Request r2 = new Request(recipeMap.get("wood"), b2, false);
    b2.requests.add(r2);

    Map<Building, GraphPath> sources = new LinkedHashMap<>();
    sources.put(b1, gp);
    sources.put(b2, gp);
    
    Building result = policy.selectSource(sources, "wood", chooseStandard);
    assertEquals(b1, result);
  }

  @Test
  public void test_estimate_singleRequest_noSubIngredients() {
    TestBuilding b = new TestBuilding("B", true);

    Request req = new Request(recipeMap.get("metal"), b, false);

    RecursiveLatPolicy.UsageInfo usage = new RecursiveLatPolicy.UsageInfo();
    RecursiveLatPolicy.Path path = new RecursiveLatPolicy.Path();
    int est = policy.estimate(req, b, usage, path);
    assertEquals(3, est);
  }

  @Test
  public void test_estimate_withSubIngredients() {
    TestBuilding b = new TestBuilding("B", true);

    Mine mineWood = new Mine("MineWood", new ArrayList<>(), recipeMap.get("wood"));
    Mine mineMetal = new Mine("MineMetal", new ArrayList<>(), recipeMap.get("metal"));

    TestBuilding bHinge = new TestBuilding("BHinge", true);
    bHinge.addSource(mineMetal);
    b.addSource(mineWood);
    b.addSource(bHinge);

    Request req = new Request(recipeMap.get("door"), b, false);
    RecursiveLatPolicy.UsageInfo usage = new RecursiveLatPolicy.UsageInfo();
    RecursiveLatPolicy.Path path = new RecursiveLatPolicy.Path();

    int est = policy.estimate(req, b, usage, path);
    // Estimate should be at least the base door latency: 10
    assertTrue(est >= 10);
  }

  @Test
  public void test_estimate_storageFullyStocked() {
    Recipe doorRecipe = recipeMap.get("door");
    TestStorage storage = new TestStorage("S", doorRecipe, new ArrayList<>(), 100, 1.0);
    storage.addIngredient("door"); // increases storage amount by 1.
    Request req = new Request(doorRecipe, storage, false);
    RecursiveLatPolicy.UsageInfo usage = new RecursiveLatPolicy.UsageInfo();
    RecursiveLatPolicy.Path path = new RecursiveLatPolicy.Path();
    int est = policy.estimate(req, storage, usage, path);
    assertEquals(0, est, "Fully stocked storage should result in 0 latency.");
  }

  @Test
  public void test_estimate_storagePartiallyStocked() {
    Recipe doorRecipe = recipeMap.get("door");
    TestStorage storage = new TestStorage("S", doorRecipe, new ArrayList<>(), 100, 1.0);
    TestBuilding factory = new TestBuilding("F1", true);
    storage.addSource(factory);
    Request req = new Request(doorRecipe, storage, false);
    RecursiveLatPolicy.UsageInfo usage = new RecursiveLatPolicy.UsageInfo();
    RecursiveLatPolicy.Path path = new RecursiveLatPolicy.Path();
    int est = policy.estimate(req, storage, usage, path);
    assertTrue(est >= 10, "Partially stocked (or empty) storage should result in latency >= base door latency.");
  }

}
