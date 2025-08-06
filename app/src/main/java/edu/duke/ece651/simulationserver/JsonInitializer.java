package edu.duke.ece651.simulationserver;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The JsonInitializer class is responsible for reading and parsing a JSON configuration
 * file to initialize the simulation components including recipes, factory types, buildings, and requests.
 * 
 * It implements the Initializer interface and provides methods to construct maps of
 * Recipe, FactoryType, and Building objects as well as the starting simulation cycle.
 * Additionally, it sets up request and source policies for buildings.
 * 
 */
public class JsonInitializer {
  private JsonNode rootNode;
  // private Map<String, Recipe> recipes;
  // private Map<String, FactoryType> types;
  // private Map<String, Building> buildings;
  // private Map<Integer, Request> requests;
  private Map<String, RequestSelectionPolicy> requestPolicies;
  private Map<String, SourceSelectionPolicy> sourcePolicies;
  private Connector connector;
  private boolean isInitializer;
  
  /**
   * Constructs a newJsonInitializer that reads the simulation configuration from the specified JSON file.
   *
   * @param jsonFileName the path to the JSON configuration file
   * @throws IOException if an I/O error occurs while reading the file
   */
  public JsonInitializer(String jsonFileName) throws IOException {
    CoordinateSetter.reset();
    ObjectMapper mapper = new ObjectMapper();
    FileInputStream fis = new FileInputStream(jsonFileName);
    connector = new Connector();
    rootNode = mapper.readTree(fis);
    isInitializer = false;
  }

  public void initializeSystem(Map<String, Recipe> recipes,
                               Map<String, FactoryType> types,
                               Map<String, Building> buildings,
                               Map<Coordinate, Square> squares,
                               List<Road> roads) {
    initializeRecipes(recipes);
    initializeTypes(types, recipes);
    initialRoads(squares, roads);
    initializeBuildings(recipes, types, buildings, squares, roads);
  }

  /**
   * Creates and returns a mapping of request selection policies.
   *
   * @return a map from policy names (e.g., "fifo", "ready", "sjf") to their corresponding RequestSelectionPolicy objects
   */
  private Map<String, RequestSelectionPolicy> createRequestPolicies() {
    Map<String, RequestSelectionPolicy> policies = new HashMap<>();
    policies.put("fifo", new FifoPolicy());
    policies.put("ready", new ReadyPolicy());
    policies.put("sjf", new SjfPolicy());
    return policies;
  }

  /**
   * Creates and returns a mapping of source selection policies.
   *
   * @return a map from policy names (e.g., "qlen", "simpleLat", "recursiveLat") to their corresponding SourceSelectionPolicy objects
   */
  private Map<String, SourceSelectionPolicy> createSourcePolicies(Map<String, Recipe> recipes) {
    Map<String, SourceSelectionPolicy> policies = new HashMap<>();
    policies.put("qlen", new QlenPolicy());
    policies.put("simpleLat", new SimplelatPolicy());
    policies.put("recursiveLat", new RecursiveLatPolicy(recipes));
    return policies;
  }

  /**
   * Initializes the recipes from the JSON configuration.
   * 
   * Each recipe is expected to be defined with an "output", "latency", and an "ingredients" object.
   * The returned map uses recipe outputs as keys.
   * 
   *
   * @return a Map mapping recipe outputs to their corresponding Recipe objects
   */
  private void initializeRecipes(Map<String, Recipe> recipes) {
    JsonNode recipesNode = rootNode.get("recipes");
    for (JsonNode recipeNode : recipesNode) {
      String output = recipeNode.get("output").asText();
      int latency = recipeNode.get("latency").asInt();
      LinkedHashMap<String, Integer> ingredients = new LinkedHashMap<>();
      JsonNode ingredientsNode = recipeNode.get("ingredients");
      Iterator<String> fieldNames = ingredientsNode.fieldNames();
      while (fieldNames.hasNext()) {
        String field = fieldNames.next();
        ingredients.put(field, ingredientsNode.get(field).asInt());
      }
      if (recipeNode.get("waste") == null) {
        recipes.put(output, new Recipe(output, ingredients, latency));
      } else {
        String waste = recipeNode.get("waste").asText();
        int wasteAmount = recipeNode.get("wasteAmount").asInt();
        recipes.put(output, new Recipe(output, waste, wasteAmount, ingredients, latency));
      }
    }
  }
  
  /**
   * Initializes the factory types from the JSON configuration.
   * 
   * Each type is defined by a "name" and an array of recipe names. The method looks up
   * the corresponding Recipe objects from the recipes map.
   * 
   *
   * @return a Map mapping factory type names to their corresponding FactoryType objects
   */
  private void initializeTypes(Map<String, FactoryType> types, Map<String, Recipe> recipes) {
    JsonNode typesNode = rootNode.get("types");
    for (JsonNode typeNode : typesNode) {
      String name = typeNode.get("name").asText();
      //FactoryType type = new FactoryType(name);
      List<Recipe> recipes_ = new ArrayList<>();
      JsonNode typeRecipesNode = typeNode.get("recipes");
      for (JsonNode recipeNode : typeRecipesNode) {
        String recipeString = recipeNode.asText();
        recipes_.add(recipes.get(recipeString));
      }
      FactoryType type = new FactoryType(name, recipes_);
      types.put(name, type);
    }
  }

  /**
   * Initializes the requests from the JSON configuration.
   * 
   * Each request is defined by an "id", "recipe", "requester", "state", and "isUserRequest" flag.
   * The method also sets the request ID generator to the value specified in the JSON.
   * 
   */
  private Map<Integer, Request> initializeRequests(Map<String, Building> buildings, Map<String, Recipe> recipes) {
    Map<Integer, Request> requests = new HashMap<>();
    JsonNode requestsNode = rootNode.get("requests");
    for (JsonNode requestNode : requestsNode) {
      int id = requestNode.get("id").asInt();
      if (requestNode.get("amount") != null ) {
        int amount = requestNode.get("amount").asInt();
        Building requester = buildings.get(requestNode.get("requester").asText());
        String state = requestNode.get("state").asText();
        Request request = new wasteRequest(id, (WasteDisposal)requester, state, amount);
        requests.put(id, request);
      } else {
        Recipe recipe = recipes.get(requestNode.get("recipe").asText());
        Building requester = null;
        if (requestNode.has("requester")) {
          requester = buildings.get(requestNode.get("requester").asText());
        }
        String state = requestNode.get("state").asText();
        Boolean userRequest = requestNode.get("isUserRequest").asBoolean();
        Request request = new Request(id, recipe, requester, state, userRequest);
        requests.put(id, request);
      }
    }
    int generatorId = rootNode.get("requestId").asInt();
    Request.setIdGenerator(generatorId);
    return requests;
  }
  
  /**
   * Initializes the buildings from the JSON configuration.
   * 
   * For each building defined in the JSON, the building is instantiated either as a Factory or a Mine based on
   *  whether there is a "type" field or a "mine" field. Optional fields such as "time", "inventory", 
   * "requestSelectPolicy", "sourceSelectPolicy", "defaultRequestPolicy", and "defaultSourcePolicy" are also processed.
   * 
   * After creating the building objects, this method calls helper methods to add source
   * references and link any requests.
   * 
   *
   * @return a Map mapping building names to their corresponding Building objects
   */
  private void initializeBuildings(Map<String, Recipe> recipes,
                                  Map<String, FactoryType> types,
                                   Map<String, Building> buildings,
                                   Map<Coordinate, Square> squares,
                                   List<Road> roads) {
    requestPolicies = createRequestPolicies();
    sourcePolicies = createSourcePolicies(recipes);

    JsonNode buildingsNode = rootNode.get("buildings");
    for (JsonNode buildingNode : buildingsNode) {
      String name = buildingNode.get("name").asText();
      Building building;
      if (buildingNode.has("type")) {
        String typeString = buildingNode.get("type").asText();
        FactoryType type = types.get(typeString);
        if (type == null) {
          throw new IllegalArgumentException("Factory Type doesn't exist");
        }
        building = new Factory(name, new ArrayList<>(), type);
        JsonNode wastesNode = buildingNode.get("wastes");
        if (wastesNode != null && wastesNode.isArray()) {
          for (JsonNode pair : wastesNode) {
            if (pair.isArray() && pair.size() == 2) {
              String wasteType = pair.get(0).asText();
              int amount = pair.get(1).asInt();
              ((Factory) building).addWaste(wasteType, amount);
            }
          }
        }
      }
      else if (buildingNode.has("mine")) {
        String recipeString = buildingNode.get("mine").asText();
        building = new Mine(name, new ArrayList<>(), recipes.get(recipeString));
      }
      else if (buildingNode.has("stores")) {
        int capacity = buildingNode.get("capacity").asInt();
        int remain = buildingNode.get("remain").asInt();
        double priority = buildingNode.get("priority").asDouble();
        String storesString = buildingNode.get("stores").asText();
        building = new Storage(name, recipes.get(storesString), new ArrayList<>(), capacity, remain, priority);
      } else if (buildingNode.has("interval")) {
        int capacity = buildingNode.get("capacity").asInt();
        int disposeAmount = buildingNode.get("disposeAmount").asInt();
        int disposeInterval = buildingNode.get("disposeInterval").asInt();
        int interval = buildingNode.get("interval").asInt();
        int predictedAmount = buildingNode.get("predictedAmount").asInt();
        int currentAmount = buildingNode.get("currentAmount").asInt();
        List<Recipe> wasteTypes = new ArrayList<>();
        JsonNode wasteTypesNode = buildingNode.get("wasteTypes");
        for (JsonNode r : wasteTypesNode) {
          String recipeName = r.asText();
          wasteTypes.add(recipes.get(recipeName));
        }
        building = new WasteDisposal(name, capacity, wasteTypes, disposeAmount, disposeInterval, currentAmount, interval, predictedAmount);
      }
      // DronePort building
      else {
        building = new DronePort(name);
      }
      if (buildingNode.has("time")) {
        JsonNode timeLeftNode = buildingNode.get("time");
        building.setTimeLeft(timeLeftNode.asInt());
      }
      if (buildingNode.has("inventory")) {
        JsonNode inventoryNode = buildingNode.get("inventory");
        Map<String, Integer> inventory = new HashMap<>();
        Iterator<String> fieldNames = inventoryNode.fieldNames();
        while (fieldNames.hasNext()) {
          String field = fieldNames.next();
          inventory.put(field, inventoryNode.get(field).asInt());
        }
        building.setInventory(inventory);
      }
      if (buildingNode.has("requestPolicy")) {
        String policyStr = buildingNode.get("requestPolicy").asText();
        building.setRequestPolicy(requestPolicies.get(policyStr));
      }
      if (buildingNode.has("sourcePolicy")) {
        String policyStr = buildingNode.get("sourcePolicy").asText();
        building.setSourcePolicy(sourcePolicies.get(policyStr));
      }
      if (buildingNode.has("defaultRequestPolicy")) {
        building.setDefaultRequest(buildingNode.get("defaultRequestPolicy").asBoolean());
      }
      if (buildingNode.has("defaultSourcePolicy")) {
        building.setDefaultSource(buildingNode.get("defaultSourcePolicy").asBoolean());
      }
      if (buildingNode.has("removeMark")) {
        building.setRemoveMark(buildingNode.get("removeMark").asBoolean());
      }
      
      if (buildingNode.has("coordinate")) {
        JsonNode coordNode = buildingNode.get("coordinate");
         if (coordNode.isArray() && coordNode.size() == 2) {
           int row = coordNode.get(0).asInt();
           int col = coordNode.get(1).asInt();
           Coordinate coord = new Coordinate(row, col);
           building.setCoordinate(coord);
         } else {
           throw new IllegalArgumentException("invalic building coordinate");
         }
      } 
      buildings.put(name, building);
    }
    
    for (Building b : buildings.values()) {
      if (b.getCoordinate() != null) {
        squares.put(b.getCoordinate(), b);
        CoordinateSetter.setMax(b);
      }
    }

    
    for (Building b : buildings.values()) {
      if (b.getCoordinate() == null) {
        CoordinateSetter.setCoordinate(b);
        squares.put(b.getCoordinate(), b);
      }
    }
    
    if (isInitializer) {
      addSources(buildingsNode, buildings, squares, roads);
    } else {
      addSourcesAndGraphPath(buildingsNode, buildings, squares);
      addDisposalsAndGraphPath(buildingsNode, buildings, squares);
    }
    
    if (rootNode.has("requests")) {
      Map<Integer, Request> requests = initializeRequests(buildings, recipes);
      addRequests(buildingsNode, buildings, requests); 
      addDeliveries(buildingsNode, buildings, requests);
      addDrones(buildingsNode, buildings, requests);
    }
  }

  private void initialRoads(Map<Coordinate, Square> squares, List<Road> roads) {
    JsonNode roadsNode = rootNode.get("roads");
    if (roadsNode == null) {
      isInitializer = true;
      return;
    }
    for (JsonNode roadNode : roadsNode) {
      JsonNode coordinateNode = roadNode.get("coordinate");
      int row = coordinateNode.get(0).asInt();
      int col = coordinateNode.get(1).asInt();
      Coordinate coord = new Coordinate(row, col);
      JsonNode directionNode = roadNode.get("direction");
      Road road;
      if (directionNode != null) {
        int[] direction = new int[] {
          directionNode.get(0).asInt(),
          directionNode.get(1).asInt()
        };
        road = new Road(coord, direction);
      } else {
        road = new Road(coord);
      }
      roads.add(road);
      squares.put(coord, road);
    }
  }
  
  /**
   * Helper method to add source references to buildings based on the JSON configuration.
   * 
   * For each building that has a "sources" array, this method adds each source (by name) to
   * the building's source list.
   *
   * @param buildingsNode the JSON node representing the array of buildings
   */
  private void addSources(JsonNode buildingsNode, Map<String, Building> buildings, Map<Coordinate, Square> squares, List<Road> roads) {
    for (JsonNode buildingNode : buildingsNode) {
      String name = buildingNode.get("name").asText();
      if (buildingNode.has("type") || buildingNode.has("stores")) {
        Building building = buildings.get(name);
        JsonNode sourcesNode = buildingNode.get("sources");
        for (JsonNode sourceNode : sourcesNode) {
          Building source = buildings.get(sourceNode.asText());
          GraphPath gp = connector.connect(squares, source, building, roads);
          building.addSource(source, gp);
        }
      }
    }
  }

  /**
   * Helper method to add requests to buildings based on the JSON configuration.
   * 
   * This method iterates over each building node that contains a "requests" array and adds
   * the corresponding Request objects by ID to the building.
   * Additionally, if a building node has a "currReq" field, the current request is set accordingly.
   * 
   *
   * @param buildingsNode the JSON node representing the array of buildings
   */
  private void addRequests(JsonNode buildingsNode, Map<String, Building> buildings, Map<Integer, Request> requests) {
    for (JsonNode buildingNode : buildingsNode) {
      String name = buildingNode.get("name").asText();
      JsonNode requestsNode = buildingNode.get("requests");
      for (JsonNode requestNode : requestsNode) {
        int requestId = requestNode.asInt();
        buildings.get(name).onlyAddRequest(requests.get(requestId));
      }
      if (buildingNode.has("currReq")) {
        JsonNode currReqNode = buildingNode.get("currReq");
        int currReqId = currReqNode.asInt();
        buildings.get(name).setCurrReq(requests.get(currReqId));
      }
    }
  }

  private void addDeliveries(JsonNode buildingsNode, Map<String, Building> buildings, Map<Integer, Request> requests) {
    for (JsonNode buildingNode : buildingsNode) {
      String name = buildingNode.get("name").asText();
      JsonNode deliveriesNode = buildingNode.get("deliveries");
      for (JsonNode deliveryNode : deliveriesNode) {
        int id = deliveryNode.get("requestID").asInt();
        int timeLeft = deliveryNode.get("timeleft").asInt();
        Request r = requests.get(id);
        buildings.get(name).addDelivery(r, timeLeft);
      }
    }
  }

  private void addSourcesAndGraphPath(JsonNode buildingsNode, Map<String, Building> buildings, Map<Coordinate, Square> squares) {
    for (JsonNode buildingNode : buildingsNode) {
      String name = buildingNode.get("name").asText();
      if (buildingNode.has("type") || buildingNode.has("stores")) {
        Building building = buildings.get(name);
        JsonNode sourcesNode = buildingNode.get("sources");
        for (JsonNode sourceNode : sourcesNode) {
          if (!sourceNode.isArray() || sourceNode.size() != 5) {
            throw new IllegalArgumentException("Each source must be an array of 5 elements");
          }
          String sourceName = sourceNode.get(0).asText();
          int startRow = sourceNode.get(1).asInt();
          int startCol = sourceNode.get(2).asInt();
          int endRow = sourceNode.get(3).asInt();
          int endCol = sourceNode.get(4).asInt();
          Building source = buildings.get(sourceName);
          if (source == null) {
            throw new IllegalArgumentException("Source building '" + sourceName + "' not found");
          }
          Coordinate startCoord = new Coordinate(startRow, startCol);
          Coordinate endCoord = new Coordinate(endRow, endCol);
          GraphPath path = new GraphPath();
          path.addNode(source.getCoordinate(), 0);
          Square startSquare = squares.get(startCoord);
          Square endSquare = squares.get(endCoord);
          if (startSquare.getClass() == Road.class && endSquare.getClass() == Road.class) {
            Road curr = (Road) startSquare;
            while (!curr.getCoordinate().equals(endCoord)) {
              path.addNode(curr.getCoordinate(), 0);
              int[] dir = curr.getDirection();
              if (dir == null) {
                throw new IllegalArgumentException("failed to create path from '" + sourceName + "' to '" + name + "'");
              }
              Coordinate currCoord = curr.getCoordinate();
              Coordinate nextCoord = new Coordinate(currCoord.getRow() + dir[0], currCoord.getColumn() + dir[1]);
              Square nextSquare = squares.get(nextCoord);
              if (nextSquare == null || nextSquare.getClass() != Road.class) {
                throw new IllegalArgumentException("failed to create path from '" + sourceName + "' to '" + name + "'");
              }
              curr = (Road) nextSquare;
            }
            path.addNode(curr.getCoordinate(), 0);
          }
          path.addNode(building.getCoordinate(), 0);
          building.addSource(source, path);
        }
      }
    }
  }

  private void addDisposalsAndGraphPath(JsonNode buildingsNode, Map<String, Building> buildings, Map<Coordinate, Square> squares) {
    for (JsonNode buildingNode : buildingsNode) {
      String name = buildingNode.get("name").asText();
      if (buildingNode.has("type")) {
        Building building = buildings.get(name);
        JsonNode disposalsNode = buildingNode.get("wasteDisposals");
        for (JsonNode disposalNode : disposalsNode) {
          if (!disposalNode.isArray() || disposalNode.size() != 5) {
            throw new IllegalArgumentException("Each source must be an array of 5 elements");
          }
          String disposalName = disposalNode.get(0).asText();
          int startRow = disposalNode.get(1).asInt();
          int startCol = disposalNode.get(2).asInt();
          int endRow = disposalNode.get(3).asInt();
          int endCol = disposalNode.get(4).asInt();
          Building wasteDisposal = buildings.get(disposalName);
          if (wasteDisposal == null) {
            throw new IllegalArgumentException("wasteDisposal building '" + disposalName + "' not found");
          }
          Coordinate startCoord = new Coordinate(startRow, startCol);
          Coordinate endCoord = new Coordinate(endRow, endCol);
          GraphPath path = new GraphPath();
          path.addNode(wasteDisposal.getCoordinate(), 0);
          Square startSquare = squares.get(startCoord);
          Square endSquare = squares.get(endCoord);
          if (startSquare.getClass() == Road.class && endSquare.getClass() == Road.class) {
            Road curr = (Road) startSquare;
            while (!curr.getCoordinate().equals(endCoord)) {
              path.addNode(curr.getCoordinate(), 0);
              int[] dir = curr.getDirection();
              if (dir == null) {
                throw new IllegalArgumentException("failed to create path from '" + disposalName + "' to '" + name + "'");
              }
              Coordinate currCoord = curr.getCoordinate();
              Coordinate nextCoord = new Coordinate(currCoord.getRow() + dir[0], currCoord.getColumn() + dir[1]);
              Square nextSquare = squares.get(nextCoord);
              if (nextSquare == null || nextSquare.getClass() != Road.class) {
                throw new IllegalArgumentException("failed to create path from '" + disposalName + "' to '" + name + "'");
              }
              curr = (Road) nextSquare;
            }
            path.addNode(curr.getCoordinate(), 0);
          }
          path.addNode(building.getCoordinate(), 0);
          ((Factory) building).addWasteDisposal(wasteDisposal, path);
        }     
      }
    }
  }
  
  private void addDrones(JsonNode buildingsNode, Map<String, Building> buildings, Map<Integer, Request> requests) {
    for (JsonNode buildingNode : buildingsNode) {
      if (buildingNode.has("drones")) {
        String name = buildingNode.get("name").asText();
        Building portBuilding = buildings.get(name);
        if (portBuilding instanceof DronePort) {
          DronePort port = (DronePort) portBuilding;
          for (JsonNode droneNode : buildingNode.get("drones")) {
            boolean inUse = droneNode.get("inUse").asBoolean();
            if (inUse) {
              JsonNode sourceNode = droneNode.get("source");
              int row = sourceNode.get(0).asInt();
              int col = sourceNode.get(1).asInt();
              Coordinate source = new Coordinate(row, col);
              Request request = requests.get(droneNode.get("requestID").asInt());
              int currTime = droneNode.get("currTime").asInt();
              port.addDrone(new Drone(port.getCoordinate(), source, request, currTime));
            }
            else {
              port.addDrone(new Drone(port.getCoordinate()));
            }
          }
        }
        else {
          throw new IllegalArgumentException("Invalid json file: '" + name + "' has 'drones' but is not a drone port building");
        }       
      }
    }
  }
  
  /**
   * Retrieves the starting simulation cycle from the JSON configuration.
   * If the JSON does not specify a "cycle" field, this method returns 0.
   * @return the simulation cycle as defined in the JSON, or 0 if not specified
   */
  public int getCycle() {
    if (rootNode.has("cycle")) {
      return rootNode.get("cycle").asInt();
    }
    return 0;
  }
}
