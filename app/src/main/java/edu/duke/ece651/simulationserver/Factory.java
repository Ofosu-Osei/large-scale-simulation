package edu.duke.ece651.simulationserver;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Iterator;

/**
 * A Factory is a type of Building that can produce items based on
 * the recipes defined in its FactoryType. It uses a source selection
 * policy to find suitable source buildings for each ingredient required.
 */
public class Factory extends Building {
  private FactoryType type;
  private Map<String, Integer> wastes;
  private Map<WasteDisposal, GraphPath> wasteDisposals;
  
  /**
   * Constructs a new Factory with the specified name, list of source buildings,
   * and associated factory type.
   *
   * @param nameString  the unique name of this factory
   * @param sourcesList the list of buildings that can supply ingredients to this factory
   * @param type        the FactoryType that defines which recipes this factory can produce
   */
  public Factory(String nameString, List<Building> sourcesList, FactoryType type) {
    super(nameString, sourcesList);
    this.type = type;
    this.wastes = new LinkedHashMap<>();
    this.wasteDisposals = new LinkedHashMap<>();
  }

  public Factory(String nameString, List<Building> sourcesList, FactoryType type, Coordinate c) {
    super(nameString, sourcesList, c);
    this.type = type;
    this.wastes = new LinkedHashMap<>();
    this.wasteDisposals = new LinkedHashMap<>();
  }

  public Factory(Coordinate c) {
    super("testFactory", new ArrayList<>(), c);
    this.type = null;
    this.wastes = new LinkedHashMap<>();
    this.wasteDisposals = new LinkedHashMap<>();
  }
  
  public FactoryType getFactoryType() {
    return type;
  }

  public Map<String, Integer> getWastes() {
    return wastes;
  }

  public Map<WasteDisposal, GraphPath> getWasteDisposals() {
    return wasteDisposals;
  }

  public void addWasteDisposal(Building b, GraphPath gp) {
    if (b instanceof WasteDisposal) {
      wasteDisposals.put(((WasteDisposal)b), gp);
    } else {
      throw new IllegalArgumentException("invalid type for disposal");
    }
  }

  public void removeWasteDisposal(Building b) {
    if (b instanceof WasteDisposal) {
      wasteDisposals.remove((WasteDisposal)b);
    } else {
      throw new IllegalArgumentException("invalid type for removeing disposal");
    }
  }
  
  private void sendWaste() {
    for (Map.Entry<String, Integer> w : wastes.entrySet()) {
      for (Map.Entry<WasteDisposal, GraphPath> d : wasteDisposals.entrySet()) {
        if (d.getKey().canDispose(w.getKey(), w.getValue())) {
          wasteRequest r = new wasteRequest(d.getKey(), w.getValue());
          d.getKey().addRequest(r);
          addDelivery(r, d.getValue().getDistance());
          d.getKey().addPredictedAmount(w.getValue());
          wastes.remove(w.getKey());
          break;
        } 
      }
    }
  }
  
  public void addWaste(String waste, int amount) {
    if (waste != null && amount != 0) {
      wastes.put(waste, wastes.getOrDefault(waste, 0) + amount);
    }
    return;
  }

  @Override
  protected void finishRequest() {
    if (!currReq.isUserRequest()) {
      GraphPath path = getPath(currReq.getRequester());
      deliveries.put(currReq, path.getDistance());
      Verbosity.ingredientDeliveredMessage(currReq.getRecipe().getOutput(), name,currReq.getRequester().getName());
      Verbosity.PrintIsReadyMessage(currReq.getRequester().getInventory(), currReq.getRequester().getRecipes());
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
    addWaste(product.getWaste(), product.getWasteAmount());
    requests.remove(currReq);
    currReq = null;
    timeLeft = -1;
  }

  @Override
  public GraphPath getPath(Building requester) {
    if (requester instanceof WasteDisposal) {
      return wasteDisposals.get(requester);
    }
    return super.getPath(requester);
  }

  @Override
  public void step() {
    sendWaste();
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
      if (!wastes.isEmpty()) {
        throw new IllegalArgumentException("Factory " + name + " have wastes to deal with.");
      }
      work();
    }
  }

  
  /**
   * Determines whether this factory can produce the specified ingredient. It checks
   * all recipes in the associated FactoryType for a matching output name.
   *
   * @param product the name of the product (or output) to check
   * @return true if at least one recipe can produce the given ingredient;
   *         false otherwise
   */
  @Override
  public boolean mayProduce(String product) {
    List<Recipe> recipes = type.getRecipes();
    for (Recipe r : recipes) {
      if (r.getOutput().equals(product)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public String capableOf(String product) {
    if (removeMark) {
      return "being removed";
    }
    if (!wastes.isEmpty()) {
      return "cannot produce '" + product + "' because of waste: " + wastes;
    }
    List<Recipe> recipes = type.getRecipes();
    for (Recipe r : recipes) {
      if (r.getOutput().equals(product)) {
        Map<String, Integer> ingredients = r.getIngredients();
        for (Map.Entry<String, Integer> entry : ingredients.entrySet()) {
          String ingredient = entry.getKey();
          String msg = hasSourceFor(ingredient);
          if (msg != null) {
            return msg;
          }
        }
        return null;
      }
    }
    return "factory '" + name + "' cannot produce '" + product + "'";
  }
  
  private String hasSourceFor(String ingredient) {
    String err = null;
    for (Map.Entry<Building, GraphPath> entry : sources.entrySet()) {
      Building b = entry.getKey();
      if (b.mayProduce(ingredient)) {
        String msg = b.capableOf(ingredient);
        if (msg == null) {
          return null;
        }
        else {
          err = msg;
        }
      }
    }
    if (err == null) {
      return "no source for '" + ingredient + "' in factory '" + name + "'";
    }
    return err;
  }

  protected List<Recipe> getRecipes() {
    return type.getRecipes();
  }

  /**
   * Allocates sub-requests for each ingredient required by the given Request.
   * 
   * @param request the parent Request that needs ingredient sub-requests
   * @throws IllegalArgumentException if no source building is found for a required ingredient
   */
  protected void allocateSubRequest(Request request) {
    Recipe recipe = request.getRecipe();
    Map<String, Integer> ingredients = recipe.getIngredients();
    int ingredientIndex = 0;
    for (Map.Entry<String, Integer> ingredient : ingredients.entrySet()) {
      for (int i = 0; i < ingredient.getValue(); ++i) {
        Map<String, Integer> chooseStandard = new LinkedHashMap<>();
        Building source_building = sourcePolicy.selectSource(sources, ingredient.getKey(), chooseStandard);
        if (source_building == null) {
          throw new IllegalArgumentException("Can't find source building for " + ingredient.getKey());
        }
        List<Recipe> recipes = source_building.getRecipes();
        Recipe sub_recipe = findRecipe(recipes, ingredient.getKey());
        Request sub_request = new Request(sub_recipe, this, false);
        Verbosity.sourceSelectionMessage(this.getName(), sourcePolicy.getSourcePolicyName(), ingredient.getKey());
        Verbosity.sourceMessage(this.getName(), request.getRecipe().getOutput(), sourcePolicy.getSourcePolicyName(),
            ingredientIndex, ingredient.getKey(), getSources(), source_building.getName(), chooseStandard);
        Verbosity.ingredientAssignmentMessage(ingredient.getKey(), source_building.getName(), this.getName());
        source_building.addRequest(sub_request);
        ingredientIndex++;
      }
    }
  }

  public static Recipe findRecipe(List<Recipe> recipes, String recipe) {
    for (Recipe r : recipes) {
      if (r.match(recipe)) {
        return r;
      }
    }
    return null;
  }

  /**
   * Adds the specified request to this factory's queue and allocates
   * sub-requests for its required ingredients.
   *
   * @param request the Request to be added to this factory
   */
  @Override
  public void addRequest(Request request) {
    super.addRequest(request);
    allocateSubRequest(request);
  }

}
