package edu.duke.ece651.simulationserver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WasteDisposal extends Building {
  private int capacity;
  private int currentAmount;
  private final List<Recipe> wasteTypes;
  private int disposeAmount;
  private int disposeInterval;
  private int interval;
  private int predictedAmount;
    
  public WasteDisposal(String nameString, int capacity, List<Recipe> wasteTypes, int disposeAmount, int disposeInterval) {
    super(nameString, null);
    this.capacity = capacity;
    this.currentAmount = 0;
    this.disposeAmount = disposeAmount;
    this.disposeInterval = disposeInterval;
    this.interval = 0;
    this.predictedAmount = 0;
    this.wasteTypes = wasteTypes;
  }

  public WasteDisposal(String nameString, int capacity, List<Recipe> wasteTypes, int disposeAmount, int disposeInterval, int currentAmount, int interval, int predictedAmount) {
    super(nameString, null);
    this.capacity = capacity;
    this.currentAmount = currentAmount;
    this.disposeAmount = disposeAmount;
    this.disposeInterval = disposeInterval;
    this.interval = interval;
    this.predictedAmount = predictedAmount;
    this.wasteTypes = wasteTypes;
  }

  
  public WasteDisposal(String nameString, int capacity, List<Recipe> wasteTypes, int disposeAmount, int disposeInterval, Coordinate c) {
    super(nameString, null, c);
    this.capacity = capacity;
    this.currentAmount = 0;
    this.disposeAmount = disposeAmount;
    this.disposeInterval = disposeInterval;
    this.interval = 0;
    this.predictedAmount = 0;
    this.wasteTypes = wasteTypes;
  }

  public WasteDisposal(int capacity, List<Recipe> wasteTypes, int disposeAmount, int disposeInterval, Coordinate c) {
    super("testDisposal", new ArrayList<>(), c);
    this.capacity = capacity;
    this.currentAmount = 0;
    this.disposeAmount = disposeAmount;
    this.disposeInterval = disposeInterval;
    this.interval = 0;
    this.predictedAmount = 0;
    this.wasteTypes = wasteTypes;
  }

  public int getCapacity() {
    return capacity;
  }

  public int getCurrentAmount() {
    return currentAmount;
  }

  public int getDisposeAmount() {
    return disposeAmount;
  }

  public int getDisposeInterval() {
    return disposeInterval;
  }

  public int getInterval() {
    return interval;
  }

  public int getPredictedAmount() {
    return predictedAmount;
  }

  public List<Recipe> getWasteTypes() {
    return wasteTypes;
  }
  
  public void addCurrentAmount(int waste) {
    currentAmount += waste;
  }

  public void addPredictedAmount(int waste) {
    predictedAmount += waste;
  }
  
  public boolean canDispose(String waste) {
    for (Recipe r : wasteTypes) {
      if (r.getWaste().equals(waste)) {
        return true;
      }
    }
    return false;
  }

  public boolean canDispose(String waste, int amount) {
    if (predictedAmount + amount > capacity) {
      return false;
    }
    
    for (Recipe r : wasteTypes) {
      if (r.getWaste().equals(waste)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void step() {
    if (currentAmount == 0) {
      return;
    } else {
      if (++interval == disposeInterval) {
        currentAmount -= disposeAmount;
        currentAmount = currentAmount < 0 ? 0 : currentAmount;
        predictedAmount -= disposeAmount;
        predictedAmount = predictedAmount < 0 ? 0 : predictedAmount;
        interval = 0;
        return;
      }
    }
  }
 
  @Override
  protected List<Recipe> getRecipes() {
    return wasteTypes;
  }

  @Override
  public boolean mayProduce(String product) {
    for (Recipe r : wasteTypes) {
      if (r.getOutput().equals(product)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public String capableOf(String product) {
    return null;
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
      return "no source for '" + ingredient + "' in disposal '" + name + "'";
    }
    return err;
  }

  @Override
  public boolean isReadyToBeRemoved() {
    return predictedAmount == 0 && currentAmount == 0;
  }
  
}
