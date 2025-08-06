package edu.duke.ece651.simulationserver;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

public class Connector {
  private PriorityQueue<GraphPath> pathPQ;
  private Set<Coordinate> visited;

  // public Connector(Map<Coordinate, Building> buildingMap, Map<Coordinate, Path> pathMap) {
  //   buildings = buildingMap;
  //   paths = pathMap;
  //   pathPQ = new PriorityQueue<>();
  //   visited = new HashSet<>();
  // }

  public Connector() {
    pathPQ = new PriorityQueue<>(); 
    visited = new HashSet<>();
  }

  private void init(Coordinate start) {
    GraphPath startPath = new GraphPath();
    startPath.addNode(start, 0);
    pathPQ.clear();
    pathPQ.add(startPath);
    visited.clear();
  }
  //public GraphPath connect(Map<Coordinate, Square> squares, Coordinate start, Coordinate end, List<Road> roads) {
  public GraphPath connect(Map<Coordinate, Square> squares, Building startBuilding, Building endBuilding, List<Road> roads) {
    Coordinate start = startBuilding.getCoordinate();
    Coordinate end = endBuilding.getCoordinate();  
    if (start == null || end == null) {
      throw new IllegalArgumentException("invalid input for connect");
    }
    init(start);
    while (pathPQ.size() > 0) {
      // System.out.println("Q len: " + pathPQ.size());
      GraphPath gp = pathPQ.poll();
      // System.out.println("polled gp: " + gp);
      // for (GraphPath p : pathPQ) {
      //   System.out.println("path: " + p + " cost: " + p.getCost());
      // }
      Coordinate currCoord = gp.getEnd();
      if (currCoord == null) continue;
      Square currSquare = squares.get(currCoord);
      if (visited.contains(currCoord) || (currSquare instanceof Building && !currCoord.equals(start))) {
        continue;
      }
      if (currCoord.distanceTo(end) <= 1) {
        gp.addNode(end, 0);
        buildRoad(gp, squares, roads);
        return gp;
      }
      visited.add(currCoord);
      if (currSquare instanceof Road) {
        Road p = (Road) currSquare;
        int[] dir = p.getDirection();
        if (dir != null) {
          addPath(gp, new Coordinate(currCoord.getRow() + dir[0], currCoord.getColumn() + dir[1]), squares);
          continue;
        }
      }
      addPath(gp, new Coordinate(currCoord.getRow(), currCoord.getColumn() + 1), squares);
      addPath(gp, new Coordinate(currCoord.getRow(), currCoord.getColumn() - 1), squares);
      addPath(gp, new Coordinate(currCoord.getRow() + 1, currCoord.getColumn()), squares);
      addPath(gp, new Coordinate(currCoord.getRow() - 1, currCoord.getColumn()), squares);
    }
    throw new IllegalArgumentException("cannot connect building '" + startBuilding.getName() + "' to '" + endBuilding.getName() + "'");

  }

  

  private void addPath(GraphPath currPath, Coordinate nextCoord, Map<Coordinate, Square> squares) {
    if (!squares.containsKey(nextCoord)) {
      pathPQ.add(currPath.copyAndAddNode(nextCoord, 2));
    }
    else if (squares.get(nextCoord) instanceof Road) {
      pathPQ.add(currPath.copyAndAddNode(nextCoord, 1));
    }
  }

  private void buildRoad(GraphPath gp, Map<Coordinate, Square> squares, List<Road> roads) {
    List<Coordinate> coords = gp.getCoordinates();
    if (coords == null || coords.size() <= 2) {
      return;
    }
    for (int i = coords.size() - 2; i > 0; i--) {
      Coordinate coord = coords.get(i);
      if (squares.containsKey(coord)) {
        continue;
      }
      String direction;
      if (i == coords.size() - 1) {
        direction = null;
      }
      else {
        Coordinate nextCoord = coords.get(i + 1);
        if (nextCoord.getRow() > coord.getRow()) {
          direction = "u";
        }
        else if (nextCoord.getRow() < coord.getRow()) {
          direction = "d";
        }
        else if (nextCoord.getColumn() > coord.getColumn()) {
          direction = "r";
        }
        else if (nextCoord.getColumn() < coord.getColumn()) {
          direction = "l";
        }
        else {
          throw new IllegalArgumentException("invalid path to build");
        }
      }
      Road newRoad = new Road(coord, direction);
      squares.put(coord, newRoad);
      roads.add(newRoad);
    }
  }
}

