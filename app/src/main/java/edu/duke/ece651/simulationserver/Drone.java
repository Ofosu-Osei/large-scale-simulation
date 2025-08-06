package edu.duke.ece651.simulationserver;

public class Drone {
  private int speed;
  private double row;
  private double column;
  private Coordinate home;
  private boolean inUse;
  private int time;
  private Coordinate target;
  private Request request;
  private Coordinate source;
  private Coordinate destination;

  public Drone(int speed, Coordinate home) {
    this.speed = speed;
    this.home = home;
    this.row = this.home.getRow();
    this.column = this.home.getColumn();
    this.inUse = false;
    this.time = -1;
    this.target = null;
    this.request = null;
    this.source = null;
    this.destination = null;
  }

  public Drone(Coordinate home) {
    this(5, home);
  }

  public Drone(Coordinate home, Coordinate source, Request request, int currTime) {
    this(home);
    requestDelivery(source, request);
    for (int i = 0; i < currTime; i++) {
      fly();
    }
  }

  public int getSpeed() {
    return speed;
  }

  public boolean isInUse() {
    return inUse;
  }

  public double getRow() {
    return row;
  }

  public double getColumn() {
    return column;
  }

  public int getTime() {
    return time;
  }

  public Coordinate getSource() {
    return source;
  }

  public Request getRequest() {
    return request;
  }
  
  public boolean finished() {
    return request == null;
  }

  public void requestDelivery(Coordinate source, Request request) {
    if (inUse) {
      throw new IllegalArgumentException("drone already in use");
    }
    this.inUse = true;
    this.request = request;
    this.time = 0;
    this.source = source;
    this.destination = request.getRequester().getCoordinate();
    this.target = this.source;
  }

  public void fly() {
    if (!inUse) {
      throw new IllegalArgumentException("drone not in use");
    }
    time++;
    double dist = getDistance(row, column, target.getRow(), target.getColumn());
    if (dist <= speed) {
      row = target.getRow();
      column = target.getColumn();
      if (target == source) {
        target = destination;
      }
      else if (target == destination) {
        request.getRequester().addIngredient(request.getRecipe().getOutput());
        target = home;
      }
      else {
        finishUsage();
      }
    }
    else {
      row += (target.getRow() - row) * speed / dist;
      column += (target.getColumn() - column) * speed / dist;
    }
  }

  private void finishUsage() {
    this.inUse = false;
    this.time = -1;
    this.target = null;
    this.request = null;
    this.source = null;
    this.destination = null;
    this.row = home.getRow();
    this.column = home.getColumn();
  }

  private static double getDistance(double row1, double column1, double row2, double column2) {
    return Math.sqrt(Math.pow(row1 - row2, 2) + Math.pow(column1 - column2, 2));
  }
}
