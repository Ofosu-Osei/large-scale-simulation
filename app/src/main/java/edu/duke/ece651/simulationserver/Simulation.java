package edu.duke.ece651.simulationserver;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The Simulation class encapsulates the entire state and behavior of the production simulation.
 * 
 * It initializes the simulation components (buildings, recipes, factory types, policies, etc.) from a JSON
 * configuration file using a JsonInitializer. The simulation runs in discrete time cycles, during which
 * each building processes its queued requests. Users can advance the simulation by a given number of cycles,
 * issue production requests, and change selection policies. The simulation state (including the current cycle)
 * can be saved to a file.
 * 
 *  */
public class Simulation {
  private static int currentCycle = 0;
  private Map<String, Building> buildings;
  private Map<String, Recipe> recipes;
  private Map<String, FactoryType> types;
  private JsonInitializer systemInitializer;
  private Map<String, RequestSelectionPolicy> requestPolicies;
  private Map<String, SourceSelectionPolicy> sourcePolicies;
  private Map<Coordinate, Square> squares;
  private List<Road> roads;
  private Connector connector;
  private final PlacementRuleChecker placementChecker;
  private static List<DronePort> dronePorts = new ArrayList<>();
  
  /**
   * Constructs a new Simulation by loading its configuration from the specified JSON file.
   * 
   * The JSON configuration is parsed to initialize the recipes, factory types, and buildings.
   * The simulation's current cycle is set based on the configuration, and default request and source policies
   * are created.
   *
   * @param fileName the path to the JSON configuration file
   * @throws IOException if an error occurs while reading or parsing the configuration file
   */
  public Simulation(String fileName) throws IOException {
    systemInitializer = new JsonInitializer(fileName);
    // recipes = systemInitializer.initializeRecipes();
    // types = systemInitializer.initializeTypes();
    // buildings = systemInitializer.initializeBuildings();
    recipes = new LinkedHashMap<>();
    types = new LinkedHashMap<>();
    buildings = new LinkedHashMap<>();
    squares = new LinkedHashMap<>();
    roads = new ArrayList<>();
    systemInitializer.initializeSystem(recipes, types, buildings, squares, roads);
    currentCycle = systemInitializer.getCycle();
    requestPolicies = createRequestPolicies();
    sourcePolicies = createSourcePolicies();
    connector = new Connector();
    placementChecker = new NoCollisionRuleChecker(null);
    initDronePorts();
  }

  /**
   * Creates a mapping of request selection policies available for the simulation.
   * 
   * The returned map uses policy names (e.g., "fifo", "ready", "sjf") as keys.
   * 
   *
   * @return a Map of policy names to RequestSelectionPolicy objects
   */
  private Map<String, RequestSelectionPolicy> createRequestPolicies() {
    Map<String, RequestSelectionPolicy> policies = new HashMap<>();
    policies.put("fifo", new FifoPolicy());
    policies.put("ready", new ReadyPolicy());
    policies.put("sjf", new SjfPolicy());
    return policies;
  }

  /**
   * Creates a mapping of source selection policies available for the simulation.
   * 
   * The returned map uses policy names (e.g., "qlen", "simpleLat", "recursiveLat") as keys.
   *
   * @return a Map of policy names to SourceSelectionPolicy objects
   */
  private Map<String, SourceSelectionPolicy> createSourcePolicies() {
    Map<String, SourceSelectionPolicy> policies = new HashMap<>();
    policies.put("qlen", new QlenPolicy());
    policies.put("simplelat", new SimplelatPolicy());
    policies.put("recursivelat", new RecursiveLatPolicy(recipes));
    return policies;
  }

  /**
   * Returns the current simulation cycle.
   *
   * @return the current cycle number
   */
  public static int getCycle() {
    return currentCycle;
  }

  /**
   * Advances the simulation by one cycle.
   * 
   * In each cycle, the simulation increments the current cycle counter and instructs each building to process a step.
   * 
   */
  private void step() {
    currentCycle++;
    for (Building building : buildings.values()) {
      building.step();
    }
    for (Building building : buildings.values()) {
      building.deliver();
    }
  }

  /**
   * Advances the simulation by the specified number of cycles.
   *
   * @param n the number of cycles to advance and must be greater than 0
   * @throws IllegalArgumentException if n is less than or equal to 0
   */
  public void stepN(int n) {
    if (n <= 0) {
      throw new IllegalArgumentException("step number should be larger than 0");
    }
    for (int i = 0; i < n; i++) {
      step();
    }
  }

  /**
   * Runs the simulation until all buildings have finished processing their requests.
   * 
   * This method repeatedly advances the simulation by one cycle until every building indicates it has
   * finished processing its requests. Once complete, it prints a final message using Verbosity.
   * </p>
   */
  public void finish() {
    while (true) {
      Boolean finished = true;
      for (Building building : buildings.values()) {
        if (!building.finished()) {
          finished = false;
          break;
        }
      }
      if (!finished) {
        step();
        continue;
      }
      Verbosity.FinalMessage();
      return;
    }
  }

  /**
   * Processes a user request to produce an output from a specified building.
   * 
   * @param buildingName the name of the building to produce the output
   * @param outputName   the output product to be produced
   * @throws IllegalArgumentException if the building or recipe is not found
   */
  public void request(String buildingName, String outputName) {
    Building target = buildings.get(buildingName);
    Recipe recipe = recipes.get(outputName);
    if (target == null) {
      throw new IllegalArgumentException("invalid building name");
      
    }
    if (recipe == null) {
      throw new IllegalArgumentException("invalid output name");
    }
    String capableMsg = target.capableOf(outputName);
    if (capableMsg != null) {
      throw new IllegalArgumentException("request failed because: " + capableMsg);
    }
    Request request = new Request(recipe, null, true);
    target.addRequest(request);
  }

  /**
   * Sets the request selection policy for a specific building.
   *
   * @param buildingName the name of the building
   * @param policyName   the name of the request policy (e.g., "fifo", "ready", "sjf")
   * @throws IllegalArgumentException if the building or policy is not found
   */
  public void setRequestPolicy(String buildingName, String policyName) {
    Building target = buildings.get(buildingName);
    if (target == null) {
      throw new IllegalArgumentException("invalid building name");
    }
    RequestSelectionPolicy policy = requestPolicies.get(policyName);
    if (policy == null) {
        throw new IllegalArgumentException("invalid policy name"); 
    }
    target.setRequestPolicy(policy);
    target.setDefaultRequest(false);
  }

  /**
   * Sets the source selection policy for a specific building.
   *
   * @param buildingName the name of the building
   * @param policyName   the name of the source policy (e.g., "qlen", "simpleLat", "recursiveLat")
   * @throws IllegalArgumentException if the building or policy is not found
   */
  public void setSourcePolicy(String buildingName, String policyName) {
    Building target = buildings.get(buildingName);
    if (target == null) {
      throw new IllegalArgumentException("invalid building name");
    }
    SourceSelectionPolicy policy = sourcePolicies.get(policyName);
    if (policy == null) {
        throw new IllegalArgumentException("invalid policy name"); 
    }
    target.setSourcePolicy(policy);
    target.setDefaultSource(false);
  }

  /**
   * Sets the source selection policy for all buildings.
   *
   * @param policyName the name of the source policy to apply to all buildings
   * @throws IllegalArgumentException if the policy is not found
   */
  public void setSourceAll(String policyName) {
    SourceSelectionPolicy policy = sourcePolicies.get(policyName);
    if (policy == null) {
        throw new IllegalArgumentException("invalid policy name"); 
    }
    for (Map.Entry<String, Building> buildingSet : buildings.entrySet()) {
      buildingSet.getValue().setSourcePolicy(policy);
      buildingSet.getValue().setDefaultSource(false);
    }
  }

  /**
   * Sets the request selection policy for all buildings.
   *
   * @param policyName the name of the request policy to apply to all buildings
   * @throws IllegalArgumentException if the policy is not found
   */
  public void setRequestAll(String policyName) {
    RequestSelectionPolicy policy = requestPolicies.get(policyName);
    if (policy == null) {
        throw new IllegalArgumentException("invalid policy name"); 
    }
    for (Map.Entry<String, Building> buildingSet : buildings.entrySet()) {
      buildingSet.getValue().setRequestPolicy(policy);
      buildingSet.getValue().setDefaultRequest(false);
    }
  }

  /**
   * Sets the default request selection policy for buildings that are currently using the default policy.
   * 
   * Only buildings flagged as using the default request policy will have their policy updated.
   *
   * @param policyName the name of the new default request policy
   * @throws IllegalArgumentException if the policy is not found
   */
  public void setRequestDefault(String policyName) {
    RequestSelectionPolicy policy = requestPolicies.get(policyName);
    if (policy == null) {
        throw new IllegalArgumentException("invalid policy name"); 
    }
    for (Map.Entry<String, Building> buildingSet : buildings.entrySet()) {
      Building b = buildingSet.getValue();
      if (b.usingDefaultRequestPolicy()) {
        b.setRequestPolicy(policy);
      }
    }
  }

  /**
   * Sets the default source selection policy for buildings that are currently using the default policy.
   * 
   * Only buildings flagged as using the default source policy will have their policy updated.
   * 
   *
   * @param policyName the name of the new default source policy
   * @throws IllegalArgumentException if the policy is not found
   */
  public void setSourceDefault(String policyName) {
    SourceSelectionPolicy policy = sourcePolicies.get(policyName);
    if (policy == null) {
        throw new IllegalArgumentException("invalid policy name"); 
    }
    for (Map.Entry<String, Building> buildingSet : buildings.entrySet()) {
      Building b = buildingSet.getValue();
      if (b.usingDefaultSourcePolicy()) {
        b.setSourcePolicy(policy);
      }
    }
  }

  /**
   * Sets the verbosity level for the simulation's output.
   *
   * @param num the desired verbosity level
   */
  public void setVerbosity(int num) {
    Verbosity.changeVerbosity(num);
  }

  /**
   * Saves the current simulation state to a JSON file. 
   *
   * @param fileName the name (or path) of the file to save the simulation state to
   * @throws IOException if an error occurs while writing the file
   */
  public void save(String fileName) throws IOException  {
    ArrayList<Request> requests = new ArrayList<>();
    for (Map.Entry<String, Building> buildingSet : buildings.entrySet()) {
      Building b = buildingSet.getValue();
      requests.addAll(b.getRequests());
      requests.addAll(b.getDeliveries().keySet());
    }
    JsonSaver saver = new JsonSaver(recipes, types, buildings, requests, Request.getIdGenerator(), currentCycle, roads);
    saver.saveToFile(fileName);
  }
  
  /**
   * Retrieves the building with the specified name.
   *
   * @param name the name of the building
   * @return the Building with the given name, or null if not found
   */
  public Building getBuilding(String name) {
    return buildings.get(name);
  }

  /**
   * Returns the map of recipes used in the simulation.
   *
   * @return a Map mapping recipe names to Recipe objects
   */
  public Map<String, Recipe> getRecipes() {
    return recipes;
  }

  /**
   * Returns the map of factory types used in the simulation.
   *
   * @return a Map mapping factory type names to FactoryType objects
   */
  public Map<String, FactoryType> getTypes() {
    return types;
  }

  /**
   * Returns the map of buildings used in the simulation.
   *
   * @return a Map mapping building names to Building objects
   */
  public Map<String, Building> getBuildings() {
    return buildings;
  }

  public void connectTwoBuilding(String srcStr, String destStr) {
    Building src = buildings.get(srcStr);
    Building dest = buildings.get(destStr);
    if (src == null || dest == null) {
      throw new IllegalArgumentException("Invalid command2");
    }
    GraphPath gp = connector.connect(squares, src, dest, roads);

    if (src instanceof Factory && dest instanceof WasteDisposal) {
      ((Factory)src).addWasteDisposal(dest, gp);
    } else {
      dest.addSource(src, gp);
    }
  }

  public void createBuilding(String fileName) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    FileInputStream fis = new FileInputStream(fileName);
    JsonNode rootNode;
    rootNode = mapper.readTree(fis);    
    fis.close();
    String type = rootNode.get("type").asText();
    String name = rootNode.get("name").asText();
    JsonNode info = rootNode.get("info");
    Building b;
    switch (type) {
    case "storage":
      b = createStorage(name, info);
      break;
    case "mine":
      b = createMine(name, info);
      break;
    case "factory":
      b = createFactory(name, info);
      break;
    case "drone port":
      b = createDronePort(name, info);
      break;
    case "waste disposal":
      b = createDisposal(name, info);
      break;
    default:
      throw new IllegalArgumentException("Unknown building type: " + type);
    } 

    String result = placementChecker.checkPlacement(b, squares);
    if (result != null) {
      throw new IllegalArgumentException(result);
    }

    CoordinateSetter.setMax(b);
    
    buildings.put(name, b);
    squares.put(b.getCoordinate(), b);
  }
  
  private WasteDisposal createDisposal(String name, JsonNode info) {
    if (info == null || !info.has("disposeAmount") || !info.has("capacity") || !info.has("disposeInterval") || !info.has("wasteTypes")) {
      throw new IllegalArgumentException("Missing required field(s) in 'info'");
    }
    
    int capacity = info.get("capacity").asInt();
    int disposeAmount = info.get("disposeAmount").asInt();
    int disposeInterval = info.get("disposeInterval").asInt();

    JsonNode recipesNode = info.get("wasteTypes");
    List<Recipe> wasteTypes = new ArrayList<>();
    if (!recipesNode.isArray()) {
      throw new IllegalArgumentException("'recipes' must be an array of strings");
    }
    
    for (JsonNode recipeNode : recipesNode) {
      if (!recipeNode.isTextual()) {
        throw new IllegalArgumentException("Each recipe must be a string");
      }
      String recipe = recipeNode.asText();
      if (recipes.get(recipe) == null) {
        throw new IllegalArgumentException("recipe not exist");
      }
      wasteTypes.add(recipes.get(recipe));
    }

    JsonNode coordNode = info.get("coordinate");
    Integer row = null;
    Integer col = null;
    
    if (!coordNode.isArray() || coordNode.size() != 2 || !coordNode.get(0).isInt() || !coordNode.get(1).isInt()) {
      throw new IllegalArgumentException("'coordinate' must be an array of two integers");
    }
    row = coordNode.get(0).asInt();
    col = coordNode.get(1).asInt();
    
    
    return new WasteDisposal(name, capacity, wasteTypes, disposeAmount, disposeInterval, new Coordinate(row, col));
  }
  
  private Storage createStorage(String name, JsonNode info) {
    if (info == null || !info.has("stores") || !info.has("capacity") || !info.has("priority") || !info.has("coordinate")) {
      throw new IllegalArgumentException("Missing required field(s) in 'info'");
    }
    
    if (!info.get("stores").isTextual()) {
      throw new IllegalArgumentException("'stores' must be a string");
    }
    if (!info.get("capacity").isInt()) {
      throw new IllegalArgumentException("'capacity' must be an integer");
    }
    if (!info.get("priority").isNumber()) {
      throw new IllegalArgumentException("'priority' must be a number");
    }
    String stores = info.get("stores").asText();
    if (recipes.get(stores) == null) {
      throw new IllegalArgumentException("invalid stores");
    }
    int capacity = info.get("capacity").asInt();
    double priority = info.get("priority").asDouble();

    JsonNode coordNode = info.get("coordinate");
    Integer row = null;
    Integer col = null;
    
    if (!coordNode.isArray() || coordNode.size() != 2 || !coordNode.get(0).isInt() || !coordNode.get(1).isInt()) {
      throw new IllegalArgumentException("'coordinate' must be an array of two integers");
    }
    row = coordNode.get(0).asInt();
    col = coordNode.get(1).asInt();
    
    return new Storage(name, recipes.get(stores), new ArrayList<Building>(), capacity, priority, new Coordinate(row, col));    
  }
  
  private Mine createMine(String name, JsonNode info) {
    if (info == null || !info.has("mine") || !info.has("coordinate")) {
      throw new IllegalArgumentException("Missing required field(s) in 'info'");
    }
    
    if (!info.get("mine").isTextual()) {
      throw new IllegalArgumentException("'stores' must be a string");
    }
    String mine = info.get("mine").asText();
    if (recipes.get(mine) == null) {
      throw new IllegalArgumentException("invalid mine");
    }
   
    JsonNode coordNode = info.get("coordinate");
    Integer row = null;
    Integer col = null;
    
    if (!coordNode.isArray() || coordNode.size() != 2 || !coordNode.get(0).isInt() || !coordNode.get(1).isInt()) {
      throw new IllegalArgumentException("'coordinate' must be an array of two integers");
    }
    row = coordNode.get(0).asInt();
    col = coordNode.get(1).asInt();
    
    return new Mine(name, null, recipes.get(mine), new Coordinate(row, col));
  }

  private Factory createFactory(String name, JsonNode info) {
    if (info == null || !info.has("type") || !info.has("coordinate")) {
      throw new IllegalArgumentException("Missing required field(s) in 'info'");
    }
    
    if (!info.get("type").isTextual()) {
      throw new IllegalArgumentException("'stores' must be a string");
    }
    String type = info.get("type").asText();
    FactoryType t;
    if (!info.has("recipes")) {
      if (types.get(type) == null) {
        throw new IllegalArgumentException("invalid factory type");
      }
      t = types.get(type);
    } else {
      if (types.get(type) != null) {
        throw new IllegalArgumentException("type already existed");
      }
      JsonNode recipesNode = info.get("recipes");
      List<Recipe> recipesList = new ArrayList<>();
      if (!recipesNode.isArray()) {
        throw new IllegalArgumentException("'recipes' must be an array of strings");
      }
      
      for (JsonNode recipeNode : recipesNode) {
        if (!recipeNode.isTextual()) {
          throw new IllegalArgumentException("Each recipe must be a string");
        }
        String recipe = recipeNode.asText();
        if (recipes.get(recipe) == null) {
          throw new IllegalArgumentException("recipe not exist");
        }
        recipesList.add(recipes.get(recipe));
      }
      t = new FactoryType(type, recipesList); 
    }

    JsonNode coordNode = info.get("coordinate");
    Integer row = null;
    Integer col = null;
    
    if (!coordNode.isArray() || coordNode.size() != 2 || !coordNode.get(0).isInt() || !coordNode.get(1).isInt()) {
      throw new IllegalArgumentException("'coordinate' must be an array of two integers");
    }
    row = coordNode.get(0).asInt();
    col = coordNode.get(1).asInt();
    
    return new Factory(name, new ArrayList<Building>(), t, new Coordinate(row, col)); 
  }

  private DronePort createDronePort(String name, JsonNode info) {
    if (!info.has("coordinate")) {
      throw new IllegalArgumentException("Missing required field(s) in 'info'");
    }
    JsonNode coordNode = info.get("coordinate");
    if (!coordNode.isArray() || coordNode.size() != 2 || !coordNode.get(0).isInt() || !coordNode.get(1).isInt()) {
      throw new IllegalArgumentException("'coordinate' must be an array of two integers");
    }
    int row = coordNode.get(0).asInt();
    int col = coordNode.get(1).asInt();
    DronePort port = new DronePort(name, new Coordinate(row, col));
    dronePorts.add(port);
    return port;
  }

  private boolean hasRequest(Building source, Building dest) {
    Set<Request> deliveringRequest = source.getDeliveries().keySet();
    for (Request request : deliveringRequest) {
      if (request.getRequester() == dest) {
        return true;
      }
    }
    Queue<Request> requests = source.getRequests();
    for (Request request : requests) {
      if (request.getRequester() == dest) {
        return true;
      }
    }
    return false;
  }
  
  public void disconnect(String sourceString, String destString) {
    Building source = buildings.get(sourceString);
    Building dest = buildings.get(destString);
    if (source == null) {
      throw new IllegalArgumentException("source building '" + sourceString + "' not found");
    }
    if (dest == null) {
      throw new IllegalArgumentException("destination building '" + destString + "' not found");
    }
    GraphPath connection = null;
    if (source instanceof Factory && dest instanceof WasteDisposal) {
      connection = ((Factory)source).getWasteDisposals().get(dest);
    } else { 
      connection = dest.getConnection(source);
    }
    if (connection == null) {
      throw new IllegalArgumentException("connection from '" + sourceString + "' to '" + destString + "' not found");
    }
    if (hasRequest(source, dest)) {
      throw new IllegalArgumentException("cannot disconnect due to deliveries on the path");
    }
    List<Coordinate> coords = connection.getCoordinates();
    Set<Road> roads_ = new HashSet<>();
    for (int i = 1; i < coords.size() - 1; i++) {
      Coordinate c = coords.get(i);
      Square s = squares.get(c);
      if (!(s instanceof Road)) {
        throw new IllegalArgumentException("non-road square " + c + " on the path");
      }
      roads_.add((Road)s);
    }
    for (Map.Entry<String, Building> buildingEntry : buildings.entrySet()) {
      Building b = buildingEntry.getValue();
      // if (b == dest) {
      //   continue;
      // }
      Map<Building, GraphPath> bSources = b.getSourceMap();
      if (bSources != null) {
        for (Map.Entry<Building, GraphPath> sourceEntry : bSources.entrySet()) {
          if (b == dest && source == sourceEntry.getKey()) {
            continue;
          }
          GraphPath bPath = sourceEntry.getValue();
          for (Coordinate roadCoord : bPath.getCoordinates()) {
            Square bSquare = squares.get(roadCoord);
            if (bSquare instanceof Road) {
              roads_.remove((Road)bSquare);
            }
            if (roads_.isEmpty()) {
              break;
            }
          }
          if (roads_.isEmpty()) {
            break;
          }
        }
      }
    }
    if (source instanceof Factory && dest instanceof WasteDisposal) {
      ((Factory)source).removeWasteDisposal(dest);
    } else {
      dest.removeSource(source);
    }
    for (Road r : roads_) {
      this.roads.remove(r);
      this.squares.remove(r.getCoordinate());
    }
  }

  public Map<Coordinate, Square> getSquares() {
    return squares;
  }

  private void initDronePorts() {
    dronePorts.clear();
    for (Building b : buildings.values()) {
      if (b.getClass() == DronePort.class) {
        dronePorts.add((DronePort) b);
      }
    }
  }
  
  public static List<DronePort> getDronePorts() {
    return dronePorts;
  }

  public void addDrone(String dronePortName) {
    Building dronePortBuilding = buildings.get(dronePortName);
    if (dronePortBuilding == null || dronePortBuilding.getClass() != DronePort.class) {
      throw new IllegalArgumentException("Invalid command: building: " + dronePortBuilding);
    }
    DronePort dronePort = (DronePort) dronePortBuilding;
    if (!dronePort.addDrone()) {
      throw new IllegalArgumentException("Drone Port '" + dronePort.getName() + "' is full");
    }
  }
  
  public void tryRemoveBuilding(String building) {
    Building b = buildings.get(building);
    if (b == null) {
      throw new IllegalArgumentException("no such a building to remove!");
    }

    if (b.isReadyToBeRemoved()) {
      removeBuilding(building);
    } else {
      b.setRemoveMark(true);
      //throw new IllegalArgumentException("cannot remove building " + b.getName() + " now. Set removeMark.");
    }
  }

  public void removeBuilding(String building) {
    Building b = buildings.get(building);
    if (b == null) {
      throw new IllegalArgumentException("no such a building to remove!");
    }

    List<Building> sources = b.getSources();
    for (Building source : sources) {
      disconnect(source.getName(), building);
    }
    
    for (Building dest : buildings.values()) {
      if (dest.hasSource(b)) {
        disconnect(building, dest.getName());
      }
    }

    if (b instanceof Factory) {
      Set<WasteDisposal> disposals = ((Factory)b).getWasteDisposals().keySet();
      for (WasteDisposal disposal : disposals) {
        disconnect(building, disposal.getName());
      }
    }

    if (b instanceof DronePort) {
      dronePorts.remove(b);
    }

    buildings.remove(b.getName());
    squares.remove(b.getCoordinate());
  }
}
