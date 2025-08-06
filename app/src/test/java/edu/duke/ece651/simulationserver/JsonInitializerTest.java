package edu.duke.ece651.simulationserver;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.junit.jupiter.api.Test;

public class JsonInitializerTest {
  @Test
  public void test_buildingInventory() throws IOException {
    // JsonInitializer init = new JsonInitializer("src/test/resources/unknown.json");
    // init.initializeRecipes();
    // init.initializeTypes();
    // assertThrows(IllegalArgumentException.class, () -> {
    //   init.initializeBuildings();
    // });
  }

  @Test
  public void test_buildingInitAll() throws IOException {
    JsonInitializer init = new JsonInitializer("src/test/resources/buildingInv.json");

    // init.initializeRecipes();
    // init.initializeTypes();
    // Map<String, Building> result = init.initializeBuildings();
    // assertEquals(5, init.getCycle(), "Should read cycle=5 from JSON");
    // Building b1 = result.get("B1");
    // assertNotNull(b1);
    // assertEquals(10, b1.getTimeLeft());
    // assertEquals(5, b1.getInventory().get("wood"));
    // assertEquals(3, b1.getInventory().get("metal"));
    // assertEquals("fifo", b1.getRequestPolicy().getRequestPolicyName());
    // assertEquals("qlen", b1.getSourcePolicy().getSourcePolicyName());
    // assertTrue(b1.usingDefaultRequestPolicy());
    // assertFalse(b1.usingDefaultSourcePolicy());
    // assertEquals(1, b1.getSources().size());
    // assertEquals("B2", b1.getSources().get(0).getName());

    // assertFalse(b1.getRequests().isEmpty());
    // Request curr = b1.getCurrRequest();
    // assertNotNull(curr);
    // assertEquals(101, curr.getId());
    // assertEquals(b1, curr.getRequester());
    // assertEquals("WORKING", curr.getState().name());
    // assertTrue(curr.isUserRequest());
    // Building b2 = result.get("B2");
    // assertNotNull(b2);
    // assertTrue(b2 instanceof Mine);
    // assertTrue(b2.getSources().isEmpty());
  }

  @Test
  public void test_error_type() throws IOException {
    JsonInitializer initializer = new JsonInitializer("src/test/resources/invalid-type.json");
    assertThrows(IllegalArgumentException.class, () -> initializer.initializeSystem(new HashMap<>(), new HashMap<>(), new HashMap<>(), new LinkedHashMap<>(), new ArrayList<>()));
  }

}
