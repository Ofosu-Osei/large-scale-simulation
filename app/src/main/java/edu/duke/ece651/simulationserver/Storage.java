package edu.duke.ece651.simulationserver;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Storage extends Building {
  private Recipe stores;
  private int capacity;
  private double priority;
  private int frequency;
  private int remain;
  private int amount;

  public Storage(String nameString, Recipe storesRecipe, List<Building> sourcesList, int cap, double pri) {
    this(nameString, storesRecipe, sourcesList, cap, pri, null);
  }

  public Storage(String nameString, Recipe storesRecipe, List<Building> sourcesList, int cap, int re, double pri) {
    this(nameString, storesRecipe, sourcesList, cap, re, pri, null);
  }

  public Storage(String nameString, Recipe storesRecipe, List<Building> sourcesList, int cap, double pri, Coordinate c) {
    super(nameString, sourcesList, c);
    stores = storesRecipe;
    capacity = cap;
    priority = pri;
    remain = capacity;
    amount = 0;
    frequency = getFreq(amount, remain, priority);
  }

  public Storage(String nameString, Recipe storesRecipe, List<Building> sourcesList, int cap, int re, double pri, Coordinate c) {
    super(nameString, sourcesList, c);
    stores = storesRecipe;
    capacity = cap;
    priority = pri;
    remain = re;
    amount = 0;
    frequency = getFreq(amount, remain, priority);
  }

  
  private static int getFreq(int amount, int remain, double priority) {
    if (remain == 0 || priority == 0) {
      return -1;
    }
    double amt = (double)amount;
    double rem = (double)remain;
    double ans = Math.ceil(amt * amt / rem / priority);
    return (int)ans;
  }

  private void updateFreq() {
    frequency = getFreq(amount, remain, priority);
  }

  private void makeRequest() {
    Map<String, Integer> chooseStandard = new LinkedHashMap<>();
    Building source_building = sourcePolicy.selectSource(sources, stores.getOutput(), chooseStandard);
    if (source_building == null) {
      //throw new IllegalArgumentException("Can't find source building for " + stores.getOutput());
      return;
    }
    Request request = new Request(stores, this, false);
    source_building.addRequest(request);
    remain--;
  }

  // @Override
  // public void deliver() {
  //   while ((!finished()) && (amount > 0)) {
  //     amount--;
  //     Request request = requests.remove();
  //     Building requester = request.getRequester();
  //     if (requester != null) {
  //       requester.addIngredient(request.getRecipe().getOutput());
  //     }
  //   }
  // }

  @Override
  public void step() {
    updateFreq();
    if (frequency >= 0 && (frequency == 0 || Simulation.getCycle() % frequency == 0)) {
      makeRequest();
    }
    while ((!finished()) && (amount > 0)) {
      amount--;
      Request request = requests.remove();
      if (!request.isUserRequest()) {
        GraphPath path = getPath(currReq.getRequester());
        deliveries.put(request, path.getDistance());
      }
    }
  }
  
  @Override
  public void addRequest(Request request) {
    super.addRequest(request);
    remain++;
  }

  @Override
  protected List<Recipe> getRecipes() {
    ArrayList<Recipe> ans = new ArrayList<>();
    ans.add(stores);
    return ans;
  }

  @Override
  public boolean mayProduce(String product) {
    return product.equals(stores.getOutput());
  }

  @Override
  public String capableOf(String product) {
    if (removeMark) {
      return "being removed";
    }
    if (!product.equals(stores.getOutput())) {
      return "storage '" + name + "' cannot provide '" + product + "'";
    }
    else {
      return hasSourceFor(stores.getOutput());
    }
  }

  @Override
  public boolean isReadyToBeRemoved() {
    return finished() && (amount == 0 && remain == capacity); 
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

  @Override
  public void addIngredient(String ingredient) {
    if (!ingredient.equals(stores.getOutput())) {
      throw new IllegalArgumentException("adding invalid product");
    }
    amount++;
  }

  public int getCapacity() {
    return capacity;
  }

  public double getPriority() {
    return priority;
  }
  
  public int getFreq() {
    return frequency;
  }

  public int getRemain() {
    return remain;
  }

  public int getAmount() {
    return amount;
  }

  public Recipe getRecipe() {
    return stores;
  }
  
  @Override
  public int getQlen() {
    return getHowManyRequests() - amount;
  }

  @Override
  public Integer getSimplelat() {
    return getTotalLatence() - amount * stores.getLatency();
  }
}
