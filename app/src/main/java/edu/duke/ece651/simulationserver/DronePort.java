package edu.duke.ece651.simulationserver;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class DronePort extends Building {
  private int limit;
  private List<Drone> drones;

  public DronePort(String nameString, Coordinate coord, int lim) {
    super(nameString, null, coord);
    limit = lim;
    drones = new ArrayList<>();
  }

  public DronePort(String nameString, Coordinate coord) {
    this(nameString, coord, 10);
  }

  public DronePort(String nameString) {
    this(nameString, null);
  }

  // public DronePort(String nameString, List<Drone> drones) {
  //   this(nameString);
  //   this.drones = drones;
  // }

  public List<Drone> getDrones() {
    return drones;
  }

  public boolean addDrone() {
    if (drones.size() < limit) {
      drones.add(new Drone(coordinate));
      return true;
    }
    else {
      return false;
    }
  }

  public void addDrone(Drone drone) {
    if (drones.size() >= limit) {
      throw new IllegalArgumentException("drone port '" + name + "' is full");
    }
    drones.add(drone);
  }

  @Override
  public void step() {}

  @Override
  public void deliver() {
    for (Drone drone : drones) {
      if (drone.isInUse()) {
        drone.fly();
      }
    }
  }

  @Override
  public Queue<Request> getRequests() {
    Queue<Request> ans = new LinkedList<>();
    for (Drone drone : drones) {
      if (drone.isInUse()) {
        ans.add(drone.getRequest());
      }
    }
    return ans;
  }

  @Override
  public boolean finished() {
    return getRequests().isEmpty();
  }

  public List<Recipe> getRecipes() {
    return null;
  }

  public String capableOf(String product) {
    return "drone port " + name + " cannot produce anything";
  }

  public boolean mayProduce(String product) {
    return false;
  }

  public boolean useDrone(Building source, Request request) {
    if (!inRange(source, request.getRequester())) {
      return false;
    }
    for (Drone drone : drones) {
      if (!drone.isInUse()) {
        drone.requestDelivery(source.getCoordinate(), request);
        return true;
      }
    }
    return false;
  }
  
  private boolean inRange(Building source, Building destination) {
    Coordinate src = source.getCoordinate();
    if (Math.abs(src.getRow() - coordinate.getRow()) + Math.abs(src.getColumn() - coordinate.getColumn()) > 20) {
      return false;
    }
    Coordinate dest = destination.getCoordinate();
    if (Math.abs(dest.getRow() - coordinate.getRow()) + Math.abs(dest.getColumn() - coordinate.getColumn()) > 20) {
      return false;
    }
    return true;
  }
}
