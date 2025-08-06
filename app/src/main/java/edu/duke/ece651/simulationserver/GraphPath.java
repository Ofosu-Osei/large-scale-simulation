package edu.duke.ece651.simulationserver;

import java.util.ArrayList;
import java.util.List;

public class GraphPath implements Comparable<GraphPath>{
  private List<Coordinate> coordinates;
  int cost;

  public GraphPath() {
    coordinates = new ArrayList<>();
    cost = 0;
  }

  public GraphPath(List<Coordinate> coords, int c) {
    coordinates = coords;
    cost = c;
  } 

  @Override
  public int compareTo(GraphPath gp) {
    int costDiff = this.cost - gp.cost;
    if (costDiff != 0) {
      return costDiff;
    }
    int distDiff = this.getDistance() - gp.getDistance();
    if (costDiff != 0) {
      return distDiff;
    }
    int turnsDiff = this.getTurnNum() - gp.getTurnNum();
    return turnsDiff;
  }

  public int getTurnNum() {
    int ans = 0;
    if (getDistance() <= 2) {
      return ans;
    }
    boolean vertical = coordinates.get(0).getColumn() == coordinates.get(1).getColumn();    
    for (int i = 2; i < coordinates.size(); i++) {
      if (vertical) {
        if (coordinates.get(i).getColumn() != coordinates.get(i - 1).getColumn()) {
          ans++;
          vertical = false;
        }
      }
      else {
        if (coordinates.get(i).getRow() != coordinates.get(i - 1).getRow()) {
          ans++;
          vertical = true;
        }
      }
    }
    return ans;
  }

  public GraphPath copyAndAddNode(Coordinate coord, int edgeCost) {
    ArrayList<Coordinate> newCoord = new ArrayList<>(coordinates);
    newCoord.add(coord);
    return new GraphPath(newCoord, cost + edgeCost);
  }

  public void addNode(Coordinate coord, int edgeCost) {
    coordinates.add(coord);
    cost += edgeCost;
  }

  public Coordinate getFirst() {
    if (coordinates.isEmpty()) {
      return null;
    }
    return coordinates.get(0);
  }
  
  public Coordinate getEnd() {
    if (coordinates.isEmpty()) {
      return null;
    }
    return coordinates.get(coordinates.size() - 1);
  }

  public Coordinate getSecond() {
    if (coordinates.size() <= 1) {
      return null;
    }
    return coordinates.get(1);
  }
  
  public Coordinate getSecondLast() {
    if (coordinates.size() <= 1) {
      return null;
    }
    return coordinates.get(coordinates.size() - 2);
  }

  @Override
  public String toString() {
    String ans = "";
    String delim = "";
    for (Coordinate c : coordinates) {
      ans += delim + c;
      delim = " -> ";
    }
    return ans;
  }

  public int getCost() {
    return cost;
  }

  public int getDistance() {
    int ans = coordinates.size() - 2;
    return ans >= 0 ? ans : 0;
  }
  
  public List<Coordinate> getCoordinates() {
    return coordinates;
  }
}
