
package edu.duke.ece651.simulationserver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MineTest {
  private Mine woodMine;
  private Recipe woodRecipe;
  private List<Building> emptySources;

  @BeforeEach
  public void setUp() {
    // Create a sample recipe for "wood"
    LinkedHashMap<String, Integer> woodIng = new LinkedHashMap<>();
    woodRecipe = new Recipe("wood", woodIng, 2);

    // Mines usually have no sources, we pass an empty list
    emptySources = new ArrayList<>();

    woodMine = new Mine("WoodMine", emptySources, woodRecipe);
  }

  @Test
  public void test_mineConstructor() {
    // Verify the name
    assertEquals("WoodMine", woodMine.getName());

    // Verify the sources list
    assertEquals(0, woodMine.getSources().size());
    List<Recipe> mineRecipes = woodMine.getRecipes();
    assertEquals(1, mineRecipes.size());
    assertSame(woodRecipe, mineRecipes.get(0));

    assertTrue(woodMine.mayProduce("wood"));
    assertFalse(woodMine.mayProduce("metal"));

  }

  // @Test
  // public void test_processRequestValid() {
  //   Request req = new Request(woodRecipe, woodMine, false);
  //   woodMine.addRequest(req);
  //   woodMine.processRequest(req);

  //   assertEquals(1, woodMine.getRequests().size());
  //   assertTrue(woodMine.getRequests().contains(req));
  // }

  // @Test
  // public void test_processRequestInvalid() {
  //   Recipe metalRecipe = new Recipe("metal", new LinkedHashMap<>(), 3);
  //   Request metalReq = new Request(metalRecipe, woodMine, false);
  //   woodMine.addRequest(metalReq);
  //   assertThrows(IllegalArgumentException.class, () -> {
  //     woodMine.processRequest(metalReq);
  //   });
  // }

  @Test
  public void test_selectRequest_noRequests() {
    assertNull(woodMine.currReq);
    woodMine.step();
    assertNull(woodMine.currReq);
  }
  /**
   * @Test
   *       public void test_selectRequest_oneRequest() {
   *       Request req = new Request(woodRecipe, woodMine, false);
   *       woodMine.addRequest(req);
   *       woodMine.step();
   * 
   *       //assertEquals(req, woodMine.currReq);
   *       assertEquals(RequestState.WORKING, req.getState());
   *       assertEquals(1, woodMine.timeLeft);
   *       }
   * 
   * @Test
   *       public void test_selectRequest_multipleRequests() {
   *       Request req1 = new Request(woodRecipe, woodMine, false);
   *       Request req2 = new Request(woodRecipe, woodMine, false);
   *       woodMine.addRequest(req1);
   *       woodMine.addRequest(req2);
   *       woodMine.step();
   *       assertEquals(req1, woodMine.currReq);
   *       assertEquals(RequestState.WORKING, req1.getState());
   *       assertEquals(1, woodMine.timeLeft);
   *       woodMine.step();
   *       woodMine.step();
   * 
   *       assertNotNull(woodMine.currReq);
   *       assertEquals(1, woodMine.getRequests().size());
   * 
   *       woodMine.step();
   *       assertNull(woodMine.currReq);
   *       assertEquals(-1, woodMine.timeLeft);
   *       }
   */
}
