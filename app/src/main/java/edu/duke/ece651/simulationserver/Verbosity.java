package edu.duke.ece651.simulationserver;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import java.util.*;

public class Verbosity {
  private static int verbosity = 0;

  public static void changeVerbosity(int newVerbosity) {
    verbosity = newVerbosity;
  }

  public static void orderCompleteMessage(int orderIndex, String ingredient) {
    String s = "[order complete] Order " + orderIndex + " completed (" + ingredient + ") at time "
        + Simulation.getCycle();
    System.out.println(s);
  }

  public static void FinalMessage() {
    String s = "Simulation completed at time-step " + Simulation.getCycle();
    System.out.println(s);
  }

  public static void ingredientAssignmentMessage(String ingredient, String source_building, String building) {
    if (verbosity == 1 || verbosity == 2) {
      String s = "[ingredient assignment]: " + ingredient + " assigned to " + source_building + " to deliver to "
          + building;
      System.out.println(s);
    }
  }

  public static void ingredientDeliveredMessage(String ingredient, String source_building, String building) {
    if (verbosity == 1 || verbosity == 2) {
      String s = "[ingredient delivered]: " + ingredient + " to " + building + " from " + source_building + " on cycle "
          + Simulation.getCycle();
      System.out.println(s);
    }
  }

  public static void PrintIsReadyMessage(Map<String, Integer> inventory, List<Recipe> recipes) {
    if (verbosity == 1 || verbosity == 2) {
      int index = 0;
      for (Recipe recipe : recipes) {
        Map<String, Integer> reqIngredients = recipe.getIngredients();
        boolean isReady = true;
        for (Map.Entry<String, Integer> entry : reqIngredients.entrySet()) {
          String ingName = entry.getKey();
          int reqAmount = entry.getValue();
          int inStock = inventory.getOrDefault(ingName, 0);
          if (inStock < reqAmount) {
            isReady = false;
          }
        }
        if (isReady) {
          String s = "    " + index + ": " + recipe.getOutput() + " is ready";
          System.out.println(s);
          index++;
        }
      }
    }
  }

  public static void SelectMessage(String s) {
    if (verbosity == 2)
      System.out.println(s);
  }

  public static void sourceSelectionMessage(String building, String sourcePolicyName, String ingredient) {
    if (verbosity == 2) {
      String s = "[source selection]: " + building + " (" + sourcePolicyName + ") has request for " + ingredient
          + " on " + Simulation.getCycle();
      System.out.println(s);
    }
  }

  public static void sourceMessage(String building, String output, String sourcePolicyName, int ingredientIndex,
      String ingredient, List<Building> sources, String source_building, Map<String, Integer> chooseStandard) {
    if (verbosity == 2) {
      String s = "[" + building + ":" + output + ":" + ingredientIndex + "] " + "For ingredient " + ingredient;
      System.out.println(s);
      for (Map.Entry<String, Integer> entry : chooseStandard.entrySet()) {
        String key = entry.getKey();
        Integer value = entry.getValue();
        System.out.println("    " + key + ": " + value);
      }
      System.out.println("    Selecting " + source_building);
    }
  }

  public static void recipeSelectionMessage(String building, String requestSelection) {
    if (verbosity == 2) {
      String s = "[recipe selection]: " + building + " has " + requestSelection + " on cycle " + Simulation.getCycle();
      System.out.println(s);
    }
  }

  public static void recipeMessage(Queue<Request> requests, Map<String, Integer> inventory, Request selectedRequest) {
    if (verbosity == 2) {
      int requestIndex = 0;
      for (Request request : requests) {
        if (request.isReady(inventory)) {
          String s = "    " + requestIndex + ": is ready";
          System.out.println(s);
        } else {
          String s = "    " + requestIndex + ": is not ready, waiting on ";
          Map<String, Integer> lackIngredients = findLackIngredients(request, inventory);
          String lackMessage = "{";
          Iterator<Map.Entry<String, Integer>> iterator = lackIngredients.entrySet().iterator();
          while (iterator.hasNext()) {
            Map.Entry<String, Integer> entry = iterator.next();
            String key = entry.getKey();
            Integer value = entry.getValue();
            if (value > 1) {
              lackMessage += value + "x " + key;
            } else {
              lackMessage += key;
            }

            if (iterator.hasNext()) {
              lackMessage += ", ";
            }
          }
          lackMessage += "}";
          s += lackMessage;
          System.out.println(s);
        }
        requestIndex++;
      }
      if (selectedRequest != null) {
        int index = 0;
        for (Request request : requests) {
          if (request.equals(selectedRequest)) {
            break;
          }
          index++;
        }
        String selectMessage = "    " + "Selecting " + index;
        System.out.println(selectMessage);
      }
    }
  }

  public static Map<String, Integer> findLackIngredients(Request request, Map<String, Integer> inventory) {
    // Check the building's inventory against the recipe's required ingredients
    Map<String, Integer> lackIngredients = new LinkedHashMap<>();
    ;
    Recipe recipe = request.getRecipe();
    Map<String, Integer> reqIngredients = recipe.getIngredients();
    for (Map.Entry<String, Integer> entry : reqIngredients.entrySet()) {
      String ingName = entry.getKey();
      int reqAmount = entry.getValue();
      int inStock = inventory.getOrDefault(ingName, 0);
      if (inStock < reqAmount) {
        lackIngredients.put(ingName, reqAmount - inStock);
      }
    }
    return lackIngredients;
  }

}
