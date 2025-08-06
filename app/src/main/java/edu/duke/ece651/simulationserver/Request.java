package edu.duke.ece651.simulationserver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class represents a production request in the simulation.
 * A request contains a recipe, a reference to the building that requested it,
 * its current state, and a list of any sub-requests required to fulfill it.
 */
public class Request {
  protected static final AtomicInteger idGenerator = new AtomicInteger(0);

  public static void resetIdGenerator() {
    idGenerator.set(0);
  }

  public static int getIdGenerator() {
    return idGenerator.get();
  }

  public static void setIdGenerator(int newId) {
    idGenerator.set(newId);
  }

  protected final int id;
  protected final Recipe recipe;
  protected final Building requester;
  protected RequestState state;
  protected final List<Request> subRequests;
  protected final boolean isUserRequest;

  /**
   * Constructs a new Request.
   *
   * @param recipe        the Recipe to produce
   * @param requester     the Building that requested the production
   * @param isUserRequest whether this request was directly initiated by a user
   */
  public Request(Recipe recipe, Building requester, boolean isUserRequest) {
    if (recipe == null) {
      throw new IllegalArgumentException("Recipe can not be null!");
    }
    this.id = idGenerator.getAndIncrement();
    this.recipe = recipe;
    this.requester = requester;
    this.state = RequestState.WAITING;
    this.subRequests = new ArrayList<>();
    this.isUserRequest = isUserRequest;
  }

  public Request(Building requester, boolean isUserRequest) {
    this.id = idGenerator.getAndIncrement();
    this.recipe = null;
    this.requester = requester;
    this.state = RequestState.WAITING;
    this.subRequests = new ArrayList<>();
    this.isUserRequest = isUserRequest;
  }
  
  /**
   * Constructs a new Request with an explicit ID, recipe, requester, state, and user request flag.
   * 
   * This constructor is primarily used when reconstructing requests from a saved state.
   * The provided state (as a String) is converted to a  RequestState.
   *
   * @param requestId     the unique identifier for the request
   * @param recipe        the Recipe for the request; must not be null
   * @param requester     the Building that originated this request; may be null if not applicable
   * @param state         the current state of the request as a String (must match a value in RequestState)
   * @param isUserRequest true if the request was directly initiated by a user or false otherwise
   * @throws IllegalArgumentException if the recipe is null or the state is invalid
   */
  public Request(int requestId, Recipe recipe, Building requester, String state, boolean isUserRequest) {
    this.id = requestId;
    this.recipe = recipe;
    this.requester = requester;
    this.state = RequestState.valueOf(state);
    this.subRequests = new ArrayList<>();
    this.isUserRequest = isUserRequest;
  }

  
  
  // Getter
  public int getId() {
    return id;
  }

  public Recipe getRecipe() {
    return recipe;
  }

  public Building getRequester() {
    return requester;
  }

  public RequestState getState() {
    return state;
  }

  public void setState(RequestState state) {
    this.state = state;
  }

  public List<Request> getSubRequests() {
    return subRequests;
  }

  public void addSubRequest(Request subRequest) {
    subRequests.add(subRequest);
  }

  public boolean isUserRequest() {
    return isUserRequest;
  }

  /**
   * Determines if this request is ready to be processed based on the current inventory.
   * 
   * A request is considered ready if:
   *   All sub-requests are in the READY state.
   *   The building's inventory has at least the required quantity for every ingredient
   *       specified in the recipe.
   *
   * @param inventory a map where keys are ingredient names and values are the quantities available
   * @return true if the request is ready or false otherwise
   */
  public boolean isReady(Map<String, Integer> inventory) {
    // If any sub-request is not yet redy, we can't start this request
    for (Request subReq : subRequests) {
      if (subReq.getState() != RequestState.READY) {
        return false;
      }
    }

    // Check the building's inventory against the recipe's required ingredients
    Map<String, Integer> reqIngredients = recipe.getIngredients();
    for (Map.Entry<String, Integer> entry : reqIngredients.entrySet()) {
      String ingName = entry.getKey();
      int reqAmount = entry.getValue();
      int inStock = inventory.getOrDefault(ingName, 0);
      if (inStock < reqAmount) {
        return false;
      }
    }

    return true;
  }

  public void finish() {
    this.state = RequestState.READY;
  }

}
