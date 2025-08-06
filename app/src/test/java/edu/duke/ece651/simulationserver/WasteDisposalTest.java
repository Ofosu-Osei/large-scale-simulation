package edu.duke.ece651.simulationserver;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.util.*;

public class WasteDisposalTest {

  private Recipe makeRecipe(String waste, String output) {
    Map<String, Integer> ingredients = new HashMap<>();
    ingredients.put(waste, 1);
    // latency = 1, wasteAmount = 1 都简单设置
    return new Recipe(output, waste, 1, ingredients, 1);
  }

  @Test
  public void testConstructorsAndGetters() {
    List<Recipe> recipes = new ArrayList<>();
    recipes.add(makeRecipe("trash", "nothing"));

    WasteDisposal wd1 = new WasteDisposal("disposal1", 100, recipes, 10, 5);
    WasteDisposal wd2 = new WasteDisposal("disposal2", 200, recipes, 20, 10, 50, 2, 30);
    WasteDisposal wd3 = new WasteDisposal("disposal3", 300, recipes, 15, 7, new Coordinate(0, 0));
    WasteDisposal wd4 = new WasteDisposal(400, recipes, 25, 12, new Coordinate(1, 1));

    assertEquals(100, wd1.getCapacity());
    assertEquals(0, wd1.getCurrentAmount());
    assertEquals(10, wd1.getDisposeAmount());
    assertEquals(5, wd1.getDisposeInterval());
    assertEquals(0, wd1.getInterval());
    assertEquals(0, wd1.getPredictedAmount());
    assertEquals(recipes, wd1.getWasteTypes());

    assertEquals(50, wd2.getCurrentAmount());
    assertEquals(2, wd2.getInterval());
    assertEquals(30, wd2.getPredictedAmount());
  }

  @Test
  public void testAddCurrentAndPredictedAmount() {
    List<Recipe> recipes = new ArrayList<>();
    recipes.add(makeRecipe("trash", "nothing"));
    WasteDisposal wd = new WasteDisposal("disposal", 100, recipes, 10, 5);

    wd.addCurrentAmount(30);
    assertEquals(30, wd.getCurrentAmount());

    wd.addPredictedAmount(20);
    assertEquals(20, wd.getPredictedAmount());
  }

  @Test
  public void testCanDispose() {
    List<Recipe> recipes = new ArrayList<>();
    recipes.add(makeRecipe("trash", "nothing"));
    WasteDisposal wd = new WasteDisposal("disposal", 50, recipes, 10, 5);

    assertTrue(wd.canDispose("trash"));
    assertFalse(wd.canDispose("plastic"));

    assertTrue(wd.canDispose("trash", 10)); // 0+10 <= 50
    assertFalse(wd.canDispose("trash", 100)); // 0+100 > 50
    assertFalse(wd.canDispose("plastic", 10));
  }

  @Test
  public void testStep() {
    List<Recipe> recipes = new ArrayList<>();
    recipes.add(makeRecipe("trash", "nothing"));
    WasteDisposal wd = new WasteDisposal("disposal", 50, recipes, 10, 3);

    // currentAmount == 0
    wd.step();
    assertEquals(0, wd.getCurrentAmount());

    // currentAmount > 0, but interval not enough
    wd.addCurrentAmount(25);
    wd.addPredictedAmount(25);
    wd.step();
    assertEquals(25, wd.getCurrentAmount());

    wd.step();
    assertEquals(25, wd.getCurrentAmount());

    wd.step(); // now interval == disposeInterval
    assertEquals(15, wd.getCurrentAmount()); // 25 - 10
    assertEquals(15, wd.getPredictedAmount());
  }

  @Test
  public void testStepWithSmallCurrentAmount() {
    List<Recipe> recipes = new ArrayList<>();
    recipes.add(makeRecipe("trash", "nothing"));
    WasteDisposal wd = new WasteDisposal("disposal", 50, recipes, 10, 1);

    wd.addCurrentAmount(5);
    wd.addPredictedAmount(5);

    wd.step(); // 5-10 -> should not be negative
    assertEquals(0, wd.getCurrentAmount());
    assertEquals(0, wd.getPredictedAmount());
  }

  @Test
  public void testMayProduceAndCapableOf() {
    List<Recipe> recipes = new ArrayList<>();
    recipes.add(makeRecipe("trash", "compost"));
    WasteDisposal wd = new WasteDisposal("disposal", 50, recipes, 10, 5);

    assertTrue(wd.mayProduce("compost"));
    assertFalse(wd.mayProduce("plastic"));

    assertEquals(null, wd.capableOf("compost"));
  }

  @Test
  public void testIsReadyToBeRemoved() {
    List<Recipe> recipes = new ArrayList<>();
    recipes.add(makeRecipe("trash", "nothing"));
    WasteDisposal wd = new WasteDisposal("disposal", 50, recipes, 10, 5);

    assertTrue(wd.isReadyToBeRemoved());

    wd.addCurrentAmount(10);
    wd.addPredictedAmount(10);
    assertFalse(wd.isReadyToBeRemoved());

    // After step() enough times
    for (int i = 0; i < 5; i++) {
      wd.step();
    }
    assertTrue(wd.isReadyToBeRemoved());
  }
}
