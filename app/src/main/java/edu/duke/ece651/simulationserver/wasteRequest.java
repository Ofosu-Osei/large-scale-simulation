package edu.duke.ece651.simulationserver;

public class wasteRequest extends Request {
  private int amount;

  public wasteRequest(WasteDisposal wd, int amount) {
    super(wd, false);
    this.amount = amount;
  }

  public wasteRequest(int requestID, WasteDisposal wd, String state, int amount) {
    super(requestID, null, wd, state, false);
    this.amount = amount;
  }

  public int getAmount() {
    return amount;
  }

  public void setAmount(int amount) {
    this.amount = amount;
  }

}
