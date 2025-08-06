package edu.duke.ece651.simulationserver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.junit.jupiter.api.Test;

public class VerbosityTest {
  @Test
  public void test_recipeMessageIndex() {
    Verbosity.changeVerbosity(2);
    Queue<Request> requests = new LinkedList<>();
    Building building = new Building("TestBuilding", new ArrayList<>()) {
      @Override
      public boolean mayProduce(String ingredient) {
        return true;
      }

        @Override
        public String capableOf(String product) {
          return null;
        }

      @Override
      protected List<Recipe> getRecipes() {
        return Collections.emptyList();
      }
    };
    Map<String, Integer> inventory = new HashMap<>();
    inventory.put("wood", 5);

    Map<String, Integer> ing1 = new HashMap<>();
    ing1.put("wood", 3);
    Recipe recipe1 = new Recipe("r1", ing1, 1);
    Request req1 = new Request(recipe1, building, false);

    Map<String, Integer> ing2 = new HashMap<>();
    ing2.put("wood", 10);
    Recipe recipe2 = new Recipe("r2", ing2, 2);
    Request req2 = new Request(recipe2, building, false);

    requests.add(req1);
    requests.add(req2);

    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outContent));

    Verbosity.recipeMessage(requests, inventory, req2);
    System.setOut(originalOut);

    String output = outContent.toString();
    assertTrue(output.contains("0: is ready"));
    assertTrue(output.contains("1: is not ready"));
    assertTrue(output.contains("Selecting 1"));
    assertTrue(building.mayProduce(null));
    assertEquals(0, building.getRecipes().size());
  }

  @Test
  public void test_sourceSelectionMessageWhenVerbosityNot2() {
    Verbosity.changeVerbosity(1);

    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outContent));
    Verbosity.sourceSelectionMessage("B", "qlen", "wood");
    System.setOut(originalOut);
    String output = outContent.toString();
    assertTrue(output.isEmpty());
  }

  @Test
  public void test_selectMessage() {
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outContent));
    Verbosity.changeVerbosity(1);
    Verbosity.SelectMessage("This should not appear");
    String output = outContent.toString();
    assertFalse(output.contains("This should not appear"));

    outContent.reset();

    Verbosity.changeVerbosity(2);
    Verbosity.SelectMessage("This should appear");
    output = outContent.toString();
    assertTrue(output.contains("This should appear"));
    // Restore System.out
    System.setOut(originalOut);
  }

  @Test
  public void test_noOutputWhenVerbosityNot2() {
    Verbosity.changeVerbosity(3);
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outContent));

    Verbosity.sourceMessage("B", "door", "qlen", 0, "wood",
        new ArrayList<>(), "SourceB", new LinkedHashMap<>());
    Verbosity.recipeSelectionMessage("B", "fifo");
    Queue<Request> requests = new LinkedList<>();
    Map<String, Integer> inventory = new HashMap<>();
    Request selected = null;
    Verbosity.recipeMessage(requests, inventory, selected);
    System.setOut(originalOut);
    String output = outContent.toString();
    assertTrue(output.isEmpty());
  }

  @Test
  public void testIngredientAssignmentMessage() {
    // 1) Prepare to capture System.out
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    System.setOut(new PrintStream(outContent));

    // 2) Verbosity=0 => Should NOT print
    Verbosity.changeVerbosity(0);
    Verbosity.ingredientAssignmentMessage("wood", "W", "D");
    String output = outContent.toString();
    assertTrue(output.isEmpty(), "No output expected at verbosity=0");
    outContent.reset();

    // 3) Verbosity=1 => Should print
    Verbosity.changeVerbosity(1);
    Verbosity.ingredientAssignmentMessage("wood", "W", "D");
    output = outContent.toString();
    assertTrue(output.contains("[ingredient assignment]: wood assigned to W to deliver to D"),
        "Expected output at verbosity=1");
    outContent.reset();

    // 4) Verbosity=2 => Should also print
    Verbosity.changeVerbosity(2);
    Verbosity.ingredientAssignmentMessage("metal", "M", "D");
    output = outContent.toString();
    assertTrue(output.contains("[ingredient assignment]: metal assigned to M to deliver to D"),
        "Expected output at verbosity=2");

    // 5) Restore System.out
    System.setOut(originalOut);

  }
}
