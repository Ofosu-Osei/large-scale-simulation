package edu.duke.ece651.simulationserver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class JsonSaverTest {

  private Map<String, Recipe> recipes;
  private Map<String, FactoryType> types;
  private Map<String, Building> buildings;
  private List<Request> requests;
  private int requestId;
  private int cycle;
  private List<Road> paths;
  
  @BeforeEach
  public void setUp() {
    cycle = 0;
    requestId = 2;
    recipes = new HashMap<>();
    types = new HashMap<>();
    buildings = new HashMap<>();
    requests = new ArrayList<>();
    paths = new ArrayList<>();
    initialize();
  }

  @Test
  public void test_saveToFile(@TempDir Path tempDir) throws Exception {
    JsonSaver saver = new JsonSaver(recipes, types, buildings, requests, requestId, cycle, paths);
    
    File outFile = tempDir.resolve("test_output.json").toFile();
    
    saver.saveToFile(outFile.getAbsolutePath());
    
    ObjectMapper mapper = new ObjectMapper();
    JsonNode root = mapper.readTree(outFile);
    
    assertTrue(root.has("requestId"), "JSON should contain 'requestId' at root");
    assertEquals(2, root.get("requestId").asInt());
    
    assertTrue(root.has("recipes"));
    JsonNode recipesNode = root.get("recipes");
    assertTrue(recipesNode.isArray(), "recipes should be a JSON array");
    assertEquals(5, recipesNode.size(), "Should have 5 recipes in JSON");

    assertTrue(root.has("types"));
    JsonNode typesNode = root.get("types");
    assertTrue(typesNode.isArray(), "types should be a JSON array");
    assertEquals(3, typesNode.size(), "Should have 3 FactoryType in JSON");

    assertTrue(root.has("buildings"));
    JsonNode buildingsNode = root.get("buildings");
    assertTrue(buildingsNode.isArray(), "buildings should be a JSON array");
    assertEquals(5, buildingsNode.size(), "Should have 5 buildings in JSON");

    boolean foundD = false;
    for (JsonNode b : buildingsNode) {
      String bName = b.get("name").asText();
      if ("D".equals(bName)) {
        foundD = true;
        assertTrue(b.has("type"), "Factory building should have 'type'");
        assertTrue(b.has("sources"));
        JsonNode sourcesArray = b.get("sources");
        assertTrue(sourcesArray.isArray());
        //assertEquals(3, sourcesArray.size());
      }
    }
    assertTrue(foundD, "Should find building 'D' in the JSON output");

    assertTrue(root.has("requests"));
    JsonNode requestsNode = root.get("requests");
    assertTrue(requestsNode.isArray(), "requests should be a JSON array");
    assertEquals(10, requestsNode.size(), "Should have 1 request in JSON");

   
  }

  private void initialize() {
    Map<String, Integer> ingredients_door = new HashMap<>();
    ingredients_door.put("wood", 1);
    ingredients_door.put("handle", 1);
    ingredients_door.put("hinge", 3);
    Recipe door = new Recipe("door", ingredients_door, 12);

    Map<String, Integer> ingredients_handle = new HashMap<>();
    ingredients_handle.put("metal", 1);
    Recipe handle = new Recipe("handle", ingredients_handle, 5);
    
    Map<String, Integer> ingredients_hinge = new HashMap<>();
    ingredients_hinge.put("metal", 1);
    Recipe hinge = new Recipe("hinge", ingredients_hinge, 1);

    Recipe wood = new Recipe("wood", null, 1);
    Recipe metal = new Recipe("metal", null, 1);

    recipes.put("door", door);
    recipes.put("handle", handle);
    recipes.put("hinge", hinge);
    recipes.put("wood", wood);
    recipes.put("metal", metal);

    FactoryType door_type = new FactoryType("door", Arrays.asList(door));
    FactoryType handle_type = new FactoryType("handle", Arrays.asList(handle));
    FactoryType hinge_type = new FactoryType("hinge", Arrays.asList(hinge));

    types.put("door", door_type);
    types.put("handle", handle_type);
    types.put("hinge", hinge_type);

    Building W = new Mine("W", null, wood);
    Building M = new Mine("M", null, metal);
    Building Ha = new Factory("Ha", Arrays.asList(M), handle_type);
    Building Hi = new Factory("Hi", Arrays.asList(M), hinge_type);
    Building D = new Factory("D", Arrays.asList(W, Hi, Ha), door_type);

    buildings.put("D", D);
    buildings.put("Ha", Ha);
    buildings.put("Hi", Hi);
    buildings.put("M", M);
    buildings.put("W", W);

    Request r0 = new Request(door, null, true);
    D.addRequest(r0);

    for (Building b : buildings.values()) {
      requests.addAll(b.getRequests());
    }

    Road p1 = new Road(new Coordinate(0, 0));
    Road p2 = new Road(new Coordinate(1, 1));
    Road p3 = new Road(new Coordinate(2, 2));
    paths.add(p1);
    paths.add(p2);
    paths.add(p3);
  }
}
