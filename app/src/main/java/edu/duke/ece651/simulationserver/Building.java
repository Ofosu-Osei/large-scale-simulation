package edu.duke.ece651.simulationserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * An abstract building that processes production requests in discrete time
 * steps.
 * Subclasses such as Factory and Mine extend Building to implement specialized
 * behavior.
 */
abstract public class Building extends Square {
  protected String name;
  protected Map<Building, GraphPath> sources;
  protected Queue<Request> requests;
  protected Map<String, Integer> inventory;
  
  protected RequestSelectionPolicy requestPolicy;
  protected SourceSelectionPolicy sourcePolicy;
  protected Boolean defaultRequestPolicy;
  protected Boolean defaultSourcePolicy;
  protected Request currReq;
  protected int timeLeft;
  protected Map<Request, Integer> deliveries;
  protected boolean removeMark;
  
  /**
   * Constructs a new Building with a name and list of source buildings.
   *
   * @param nameString  the unique name of the building
   * @param sourcesList the list of buildings that can supply resources to this
   *                    building
   */
  public Building(String nameString, List<Building> sourcesList) {
    super(null);
    name = nameString;
    sources = buildSourcesMap(sourcesList);
    requests = new LinkedList<>();
    inventory = new HashMap<>();
    requestPolicy = new FifoPolicy();
    sourcePolicy = new QlenPolicy();
    currReq = null;
    timeLeft = -1;
    defaultRequestPolicy = true;
    defaultSourcePolicy = true;
    deliveries = new LinkedHashMap<>();
    removeMark = false;
  }

  public Building(String nameString, List<Building> sourcesList, Coordinate c) {
    super(c);
    name = nameString;
    sources = buildSourcesMap(sourcesList);
    requests = new LinkedList<>();
    inventory = new HashMap<>();
    requestPolicy = new FifoPolicy();
    sourcePolicy = new QlenPolicy();
    currReq = null;
    timeLeft = -1;
    defaultRequestPolicy = true;
    defaultSourcePolicy = true;
    deliveries = new LinkedHashMap<>();
    removeMark = false;
  }
  
  private Map<Building, GraphPath> buildSourcesMap(List<Building> sources) {
    if (sources == null) return new LinkedHashMap<>();
    Map<Building, GraphPath> map = new LinkedHashMap<>();
    for (Building b : sources) {
      map.put(b, new GraphPath());
    }
    return map;
  }
  
  public String getName() {
    return name;
  }

  public List<Building> getSources() {
    return new ArrayList<>(sources.keySet());
  }

  public Map<String, Integer> getInventory() {
    return inventory;
  }

  public Map<Building, GraphPath> getSourceMap(){
    return sources;
  }

  public void setInventory(Map<String, Integer> invent) {
    inventory = invent;
  }

  public Queue<Request> getRequests() {
    return requests;
  }

  public Request getCurrRequest() {
    return currReq;
  }

  public int getTimeLeft() {
    return timeLeft;
  }

  public RequestSelectionPolicy getRequestPolicy() {
    return requestPolicy;
  }

  public SourceSelectionPolicy getSourcePolicy() {
    return sourcePolicy;
  }

  public Boolean usingDefaultRequestPolicy() {
    return defaultRequestPolicy;
  }

  public Boolean usingDefaultSourcePolicy() {
    return defaultSourcePolicy;
  }

  public void setDefaultRequest(Boolean def) {
    defaultRequestPolicy = def;
  }

  public void setDefaultSource(Boolean def) {
    defaultSourcePolicy = def;
  }
  
  public void setRequestPolicy(RequestSelectionPolicy newPolicy) {
    requestPolicy = newPolicy;
  }

  public void setSourcePolicy(SourceSelectionPolicy newPolicy) {
    sourcePolicy = newPolicy;
  }
  
  protected void addInventory(String item, int quantity) {
    inventory.put(item, inventory.getOrDefault(item, 0) + quantity);
  }

  /**
   * Selects a request from the building's queue using the current request selection policy.
   *
   * @return the selected Request if one is ready, otherwise null
   */
  protected Request selectRequest() {
    Verbosity.recipeSelectionMessage(getName(),requestPolicy.getRequestPolicyName());
    Request selectedRequest=requestPolicy.selectRequest(requests, inventory);
    Verbosity.recipeMessage(requests, inventory,selectedRequest);
    // return requestPolicy.selectRequest(requests, inventory);
    return selectedRequest;
  }

  
  public boolean finished() {
    return requests.isEmpty() && deliveries.isEmpty();
  }

  protected void work() {
    timeLeft--;
    if (timeLeft <= 0) {
      currReq.setState(RequestState.READY);
      finishRequest();
    }
  }

  protected GraphPath getPath(Building requester) {
    Map<Building, GraphPath> requesterSources = requester.getSourceMap();
    if (!requesterSources.containsKey(this)) {
      throw new IllegalArgumentException("no connection from '" + name + "' to '" + requester.getName() + "'");
    }
    GraphPath path = requesterSources.get(this);
    if (path == null) {
      throw new IllegalArgumentException("no path from the source '" + name + "' in '" + requester.getName() + "' ");
    }
    return path;
  }

  /**
   * Processes a single simulation cycle.
   * 
   */
  public void step() {
    if (finished()) {
      return;
    }
    if (currReq == null) {
      Request selectedRequest = selectRequest();
      if (selectedRequest != null && selectedRequest.isReady(inventory)) {
        startRequest(selectedRequest);
      }
    }
    if (currReq != null) {
      work();
    }
  }

  public void deliver() {
    // if (currReq != null && currReq.getState() == RequestState.READY) {
    //   finishRequest();
    // }
    Iterator<Map.Entry<Request, Integer>> it = deliveries.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry<Request, Integer> delivery = it.next();
      if (delivery.getValue() == 0) {
        Request r = delivery.getKey();

        if (r instanceof wasteRequest) {
          Building requester = r.getRequester();
          if (requester instanceof WasteDisposal) {
            ((WasteDisposal)requester).addCurrentAmount(((wasteRequest)r).getAmount());
          } else {
            throw new IllegalArgumentException("invalid building type for requester");
          }
        } else {  
          Building requester = r.getRequester();
          requester.addIngredient(r.getRecipe().getOutput());
          Verbosity.orderCompleteMessage(r.getId(), r.getRecipe().getOutput());
        }
        it.remove();
      }
      else {
        delivery.setValue(delivery.getValue() - 1);
      }
    }
  }

  public void addIngredient(String ingredient) {
   inventory.put(ingredient, inventory.getOrDefault(ingredient, 0) + 1);
  } 

  /**
   * Finishes processing the current request.
   * 
   */
  protected void finishRequest() {
    if (!currReq.isUserRequest()) {
      boolean deliverd = false;
      for (DronePort dronePort: Simulation.getDronePorts()) {
        if (dronePort.useDrone(this, currReq)) {
          deliverd = true;
          break;
        }
      }
      if (!deliverd) {
        GraphPath path = getPath(currReq.getRequester());
        deliveries.put(currReq, path.getDistance());
        Verbosity.ingredientDeliveredMessage(currReq.getRecipe().getOutput(), name,currReq.getRequester().getName());
        Verbosity.PrintIsReadyMessage(currReq.getRequester().getInventory(), currReq.getRequester().getRecipes());
      }
    }
    else {
      Verbosity.orderCompleteMessage(currReq.getId(),currReq.getRecipe().getOutput());
    }
    Recipe product = currReq.getRecipe();
    Map<String, Integer> ingredients = product.getIngredients();
    for (Map.Entry<String, Integer> ingredientSet : ingredients.entrySet()) {
      String name = ingredientSet.getKey();
      int num = ingredientSet.getValue();
      inventory.put(name, inventory.get(name) - num);
      if (inventory.get(name) <= 0) {
        inventory.remove(name);
      }
    }
    requests.remove(currReq);
    currReq = null;
    timeLeft = -1;
  }

  public void startRequest(Request request) {
    if (!requests.contains(request)) {

      throw new IllegalArgumentException("Request not in list");
    }
    currReq = request;
    timeLeft = request.getRecipe().getLatency();
    request.setState(RequestState.WORKING);
  }

  public void addRequest(Request request) {
    if(!(request instanceof wasteRequest)){  
      String product = request.getRecipe().getOutput();
      String err = capableOf(product);
      if (err != null) {
        throw new IllegalArgumentException(err);
      }
    }
    requests.add(request);
  }

  public void onlyAddRequest(Request request) {
    requests.add(request);
  }
  
  public int getHowManyRequests() {
    return requests.size();
  }

  public int getQlen() {
    return getHowManyRequests();
  }
  
  abstract public boolean mayProduce(String product);

  /**
   * check the sources to determine if the building is capable to produce the product
   * @return null if it's capable, error message if it's not capable 
   */
  abstract public String capableOf(String product);
  
  /**
   * calculate the remain latence of the whole request queue
   * If a request is inprogress, only the remaing latency is considered
   *
   * @param request the Request to process
   */
  public Integer getTotalLatence(){
    int totalLatency = 0;
    for(Request request:requests){
      if(request==currReq){
        totalLatency+=timeLeft;
      }
      else{
        totalLatency+=request.getRecipe().getLatency();
      }
    }
    return totalLatency;
  }

  public Integer getSimplelat() {
    return getTotalLatence();
  }
  
  public void addSource(Building source, GraphPath gp) {
    sources.put(source, gp);
  }

  public void addSource(Building source) {
    sources.put(source, new GraphPath());
  }

  // public Map<Building, GraphPath> getSourceAndRoad() {
  //   return sources;
  // }
  
  public void setCurrReq(Request r) {
    currReq = r;
  }

  public void setTimeLeft(int t) {
    timeLeft = t;
  }
  
  abstract protected List<Recipe> getRecipes();

  public Coordinate getRequestLocation(Request r) {
    if (!deliveries.containsKey(r)) {
      throw new IllegalArgumentException("request not in delivery");
    }
    GraphPath path = getPath(r.getRequester());
    int deliveryTime = path.getDistance() - deliveries.get(r);
    List<Coordinate> coordinates = path.getCoordinates();
    return coordinates.get(deliveryTime);
  }

  public void addDelivery(Request r, int i) {
    if (deliveries.get(r) != null) {
      throw new IllegalArgumentException("request already in deliveries");
    }
    deliveries.put(r, i);
  }

  public Map<Request, Integer> getDeliveries() {
    return deliveries;
  }

  public GraphPath getConnection(Building source) {
    return sources.get(source);
  }

  public void removeSource(Building source) {
    if (!sources.keySet().contains(source)) {
      throw new IllegalArgumentException("source '" + source.getName() + "' not in '" + name + "'");
    }
    sources.remove(source);
  }

  @Override
  public String toString() {
    return name;
  }

  public boolean isReadyToBeRemoved() {
    return finished();
  }

  public boolean getRemoveMark() {
    return removeMark;
  }
  
  public void setRemoveMark(boolean b) {
    removeMark = b;
  }

  public boolean hasSource(Building b) {
    return sources.get(b) != null;
  }
}
