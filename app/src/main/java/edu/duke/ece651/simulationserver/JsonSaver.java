package edu.duke.ece651.simulationserver;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The JsonSaver class is responsible for saving the current state of the simulation
 * to a JSON file. It serializes the simulation data including recipes, factory types, buildings,
 * and requests, along with metadata such as the current simulation cycle and the request ID generator value.
 * 
 * This class uses Jackson's ObjectMapper to create a formatted JSON representation of the simulation state.
 * 
 */
public class JsonSaver {
  private Map<String, Recipe> recipes;
  private Map<String, FactoryType> types;
  private Map<String, Building> buildings;
  private List<Request> requests;
  private int requestId;
  private int cycle;
  private List<Road> paths;
  
  /**
   * Constructs a new JsonSaver with the given simulation data.
   *
   * @param recipes   a map of recipe outputs to Recipe objects representing the simulation's recipes
   * @param types     a map of factory type names to FactoryType objects used in the simulation
   * @param buildings a map of building names to Building objects representing all buildings in the simulation
   * @param requests  a list of Request objects representing all the requests in the simulation
   * @param requestId the current request ID value (used to initialize the request ID generator)
   * @param cycle     the current simulation cycle
   */
  public JsonSaver(Map<String, Recipe> recipes,
                   Map<String, FactoryType> types,
                   Map<String, Building> buildings,
                   List<Request> requests,
                   int requestId,
                   int cycle,
                   List<Road> paths) {
    this.recipes = recipes;
    this.types = types;
    this.buildings = buildings;
    this.requests = requests;
    this.requestId = requestId;
    this.cycle = cycle;
    this.paths = paths;
  }

  /**
   * Saves the current simulation state to a JSON file with the specified file name.
   * 
   * The JSON output includes the current request ID, simulation cycle, and arrays for recipes, types,
   * buildings, and requests. Each building's state (such as its inventory, policies, and current request)
   * is also included.
   *
   * @param fileName the name (or path) of the file to save the simulation state to
   * @throws IOException if an I/O error occurs while writing the file
   */
  public void saveToFile(String fileName) throws IOException {
    ObjectMapper mapper = new ObjectMapper();

    ObjectNode rootNode = mapper.createObjectNode();

    rootNode.put("requestId", requestId);

    rootNode.put("cycle", cycle);
    
    ArrayNode recipesArray = createRecipesArray(mapper);
    rootNode.set("recipes", recipesArray);

    ArrayNode typesArray = createTypesArray(mapper);
    rootNode.set("types", typesArray);

    ArrayNode buildingsArray = createBuildingsArray(mapper);
    rootNode.set("buildings", buildingsArray);

    ArrayNode requestsArray = createRequestsArray(mapper);
    rootNode.set("requests", requestsArray);

    ArrayNode pathsArray = createPathsArray(mapper);
    rootNode.set("roads", pathsArray);
    
    mapper.writerWithDefaultPrettyPrinter().writeValue(new File(fileName), rootNode);
  }
  
  /**
   * Creates a JSON array representing all recipes in the simulation.
   *
   * @param mapper the ObjectMapper used for creating JSON nodes
   * @return an ArrayNode where each element is a JSON object representing a recipe
   */
  private ArrayNode createRecipesArray(ObjectMapper mapper) {
    ArrayNode recipesArray = mapper.createArrayNode();
    for (Recipe recipe : recipes.values()) {
      ObjectNode recipeNode = mapper.createObjectNode();
      recipeNode.put("output", recipe.getOutput());
      recipeNode.put("latency", recipe.getLatency());
      recipeNode.put("waste", recipe.getWaste());
      recipeNode.put("wasteAmount", recipe.getWasteAmount());
      
      ObjectNode ingredientsNode = mapper.createObjectNode();
      for (Map.Entry<String, Integer> entry : recipe.getIngredients().entrySet()) {
        ingredientsNode.put(entry.getKey(), entry.getValue());
      }
      recipeNode.set("ingredients", ingredientsNode);

      recipesArray.add(recipeNode);
    }
    return recipesArray;
  }

  /**
   * Creates a JSON array representing all factory types in the simulation.
   *
   * @param mapper the ObjectMapper used for creating JSON nodes
   * @return an ArrayNode where each element is a JSON object representing a factory type,
   *         including its name and the outputs of its associated recipes
   */
  private ArrayNode createTypesArray(ObjectMapper mapper) {
    ArrayNode typesArray = mapper.createArrayNode();
    for (FactoryType type : types.values()) {
      ObjectNode typeNode = mapper.createObjectNode();
      typeNode.put("name", type.getName());
      ArrayNode typeRecipesArray = mapper.createArrayNode();
      for (Recipe recipe : type.getRecipes()) {
        typeRecipesArray.add(recipe.getOutput()); 
      }
      typeNode.set("recipes", typeRecipesArray);

      typesArray.add(typeNode);
    }
    return typesArray;
  }

  /**
   * Creates a JSON array representing all buildings in the simulation.
   * 
   * For each building, the JSON object includes the building's name, type (or mine output),
   * list of sources, associated requests, inventory, policies, default policy flags, current request,
   * and time left (if applicable).
   *
   * @param mapper the ObjectMapper used for creating JSON nodes
   * @return an ArrayNode where each element is a JSON object representing a building
   */
  private ArrayNode createBuildingsArray(ObjectMapper mapper) {
    ArrayNode buildingsArray = mapper.createArrayNode();
    for (Building building : buildings.values()) {
      ObjectNode buildingNode = mapper.createObjectNode();
      buildingNode.put("name", building.getName());
      buildingNode.put("removeMark", building.getRemoveMark());
      if (building instanceof Factory) {
        Factory factory = (Factory) building;
        buildingNode.put("type", factory.getFactoryType().getName());
        ArrayNode sourcesArray = mapper.createArrayNode();
        
        for (Map.Entry<Building, GraphPath> entry : factory.getSourceMap().entrySet()) {
          Building src = entry.getKey();
          GraphPath path = entry.getValue();
          ArrayNode item = mapper.createArrayNode();
          item.add(src.getName());
          if (path == null || path.getFirst() == null) continue;
          item.add(path.getSecond().getRow());
          item.add(path.getSecond().getColumn());
          item.add(path.getSecondLast().getRow());
          item.add(path.getSecondLast().getColumn());
          sourcesArray.add(item);
        }
        buildingNode.set("sources", sourcesArray);

        ArrayNode wastesArray = mapper.createArrayNode();
        for (Map.Entry<String, Integer> entry : factory.getWastes().entrySet()) {
          ArrayNode pair = mapper.createArrayNode();
          pair.add(entry.getKey());
          pair.add(entry.getValue());
          wastesArray.add(pair);
        }
        buildingNode.set("wastes", wastesArray);
        
        ArrayNode wasteDisposalsArray = mapper.createArrayNode();
        for (Map.Entry<WasteDisposal, GraphPath> entry : factory.getWasteDisposals().entrySet()) {
          WasteDisposal src = entry.getKey();
          GraphPath path = entry.getValue();
          ArrayNode item = mapper.createArrayNode();
          item.add(src.getName());
          if (path == null || path.getFirst() == null) continue;
          item.add(path.getSecond().getRow());
          item.add(path.getSecond().getColumn());
          item.add(path.getSecondLast().getRow());
          item.add(path.getSecondLast().getColumn());
          wasteDisposalsArray.add(item);
        }
        buildingNode.set("wasteDisposals", wasteDisposalsArray);
        
      }
      else if (building instanceof Mine) { 
        Mine mine = (Mine) building;
        buildingNode.put("mine", mine.getRecipe().getOutput());
      }
      else if (building instanceof Storage) {
        Storage storage = (Storage) building;
        buildingNode.put("stores", storage.getRecipe().getOutput());
        buildingNode.put("capacity", storage.getCapacity());
        buildingNode.put("priority", storage.getPriority());
        buildingNode.put("frequency", storage.getFreq());
        buildingNode.put("remain", storage.getRemain());
        buildingNode.put("amount", storage.getAmount());
        ArrayNode sourcesArray = mapper.createArrayNode();
        //for (Building src : storage.getSources()) {
        // sourcesArray.add(src.getName());
        //}
        
        for (Map.Entry<Building, GraphPath> entry : storage.getSourceMap().entrySet()) {
          Building src = entry.getKey();
          GraphPath path = entry.getValue();
          ArrayNode item = mapper.createArrayNode();
          item.add(src.getName());
          if (path == null || path.getFirst() == null) continue;
          item.add(path.getSecond().getRow());
          item.add(path.getSecond().getColumn());
          item.add(path.getSecondLast().getRow());
          item.add(path.getSecondLast().getColumn());
          sourcesArray.add(item);
        }
        
        buildingNode.set("sources", sourcesArray);
      } else if ( building instanceof WasteDisposal) {
        WasteDisposal disposal = (WasteDisposal) building;
        buildingNode.put("capacity", disposal.getCapacity()); 
        buildingNode.put("currentAmount", disposal.getCurrentAmount());
    
        ArrayNode wasteTypesArray = mapper.createArrayNode();
        for (Recipe recipe : disposal.getWasteTypes()) {
          wasteTypesArray.add(recipe.getOutput()); 
        }
        buildingNode.set("wasteTypes", wasteTypesArray);
        buildingNode.put("disposeAmount", disposal.getDisposeAmount());
        buildingNode.put("disposeInterval", disposal.getDisposeInterval());
        buildingNode.put("interval", disposal.getInterval());
        buildingNode.put("predictedAmount", disposal.getPredictedAmount());
      }
      // DronePort building
      else {
        DronePort dronePort = (DronePort) building;
        List<Drone> drones = dronePort.getDrones();
        ArrayNode dronesArray = mapper.createArrayNode();
        for (Drone drone : drones) {
          ObjectNode droneNode = mapper.createObjectNode();
          droneNode.put("inUse", drone.isInUse());
          if (drone.isInUse()) {
            ArrayNode srcArray = mapper.createArrayNode();
            srcArray.add(drone.getSource().getRow());
            srcArray.add(drone.getSource().getColumn());
            droneNode.set("source", srcArray);
            droneNode.put("requestID", drone.getRequest().getId());
            droneNode.put("currTime", drone.getTime());
          }
          ArrayNode coordArray = mapper.createArrayNode();
          coordArray.add(drone.getRow());
          coordArray.add(drone.getColumn());
          droneNode.set("coordinate", coordArray);
          dronesArray.add(droneNode);
        }
        buildingNode.set("drones", dronesArray);
      }

      ArrayNode requestIdsArray = mapper.createArrayNode();
      Queue<Request> requests_b = building.getRequests();
      for (Request req : requests_b) {
        requestIdsArray.add(req.getId());
      }

      buildingNode.set("requests", requestIdsArray);
      Map<String, Integer> inventory = building.getInventory();
      ObjectNode inventoryNode = mapper.createObjectNode();
      for (Map.Entry<String, Integer> entry : inventory.entrySet()) {
        inventoryNode.put(entry.getKey(), entry.getValue());
      }
      buildingNode.set("inventory", inventoryNode);
      
      buildingNode.put("requestPolicy",building.getRequestPolicy().getRequestPolicyName());
      buildingNode.put("sourcePolicy", building.getSourcePolicy().getSourcePolicyName());

      buildingNode.put("defaultRequestPolicy", building.usingDefaultRequestPolicy());
      buildingNode.put("defaultSourcePolicy", building.usingDefaultSourcePolicy());
      
      if (building.getCurrRequest() != null) {
        buildingNode.put("currReq", building.getCurrRequest().getId());
        buildingNode.put("time", building.getTimeLeft());
      }

      if (building.getCoordinate() != null) {
        ArrayNode coordinateArray = mapper.createArrayNode();
        coordinateArray.add(building.getCoordinate().getRow());
        coordinateArray.add(building.getCoordinate().getColumn());
        buildingNode.set("coordinate", coordinateArray);
      }

      Map<Request, Integer> deliveries = building.getDeliveries();
      ArrayNode deliveriesArray = mapper.createArrayNode();
      
      for (Map.Entry<Request, Integer> entry : deliveries.entrySet()) {
        Request req = entry.getKey();
        if (req.getRequester() != null) {
          ObjectNode deliveryNode = mapper.createObjectNode();
          deliveryNode.put("requestID", req.getId());
          deliveryNode.put("timeleft", entry.getValue());
          deliveryNode.put("requester", req.getRequester().getName());
          Coordinate coord = building.getRequestLocation(req);
          if (coord != null) {
            ArrayNode coordArray = mapper.createArrayNode();
            coordArray.add(coord.getRow());
            coordArray.add(coord.getColumn());
            deliveryNode.set("coordinate", coordArray);
          }
          deliveriesArray.add(deliveryNode);
        }
      }
      
      buildingNode.set("deliveries", deliveriesArray);
      
      buildingsArray.add(buildingNode);
    }
    return buildingsArray;
  }

  /**
   * Creates a JSON array representing all requests in the simulation.
   * 
   * Each request JSON object includes the request ID, recipe output, requester (if any),
   * current state, and a flag indicating whether the request was initiated by a user.
   * 
   * @param mapper the ObjectMapper used for creating JSON nodes
   * @return an ArrayNode where each element is a JSON object representing a request
   */
  private ArrayNode createRequestsArray(ObjectMapper mapper) {
    ArrayNode requestsArray = mapper.createArrayNode();
    for (Request request : requests) {
      ObjectNode requestNode = mapper.createObjectNode();
      requestNode.put("id", request.getId());
      if (request instanceof wasteRequest) {
        requestNode.put("amount", ((wasteRequest) request).getAmount());
      } else {
        requestNode.put("recipe", request.getRecipe().getOutput());
      }

      if (request.getRequester() != null) {
        requestNode.put("requester", request.getRequester().getName());
      }
      
      requestNode.put("state", request.getState().name());

      requestNode.put("isUserRequest", request.isUserRequest());

      requestsArray.add(requestNode);
    }
    return requestsArray;
  }

  private ArrayNode createPathsArray(ObjectMapper mapper) {
    ArrayNode pathsArray = mapper.createArrayNode();
    for (Road path : paths) {
      ObjectNode pathNode = mapper.createObjectNode();
      ArrayNode coordinateArray = mapper.createArrayNode();
      coordinateArray.add(path.getCoordinate().getRow());
      coordinateArray.add(path.getCoordinate().getColumn());
      pathNode.set("coordinate", coordinateArray);
      int[] direction = path.getDirection();
      if (direction != null) {
        ArrayNode directionArray = mapper.createArrayNode();
        directionArray.add(direction[0]);
        directionArray.add(direction[1]);
        pathNode.set("direction", directionArray);
      }
      pathsArray.add(pathNode);
    }
    return pathsArray;
  }
}
