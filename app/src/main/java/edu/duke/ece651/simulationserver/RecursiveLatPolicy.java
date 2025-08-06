package edu.duke.ece651.simulationserver;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * The RecursiveLatPolicy class implements the SourceSelectionPolicy
 * interface to select a source building based on the estimated total latency of
 * fulfilling
 * a request. This policy recursively estimates the time needed for a building
 * to complete
 * its queued requests, including any sub-requests required to produce the
 * needed ingredient.
 * 
 * For each candidate source, it computes a total estimated time by summing the
 * latency of its
 * requests (using a recursive estimation algorithm) and then returns the
 * building with the
 * smallest total estimate. Ties are broken by the order in which the candidate
 * buildings are
 * provided.
 * 
 */
public class RecursiveLatPolicy implements SourceSelectionPolicy {
  private Map<String, Recipe> recipeMap;

  /**
   * Constructs a new {@code RecursiveLatPolicy} using the given recipe map.
   *
   * @param recipeMap a map from ingredient names to Recipe objects used for
   *                  estimating
   *                  production latency
   */
  public RecursiveLatPolicy(Map<String, Recipe> recipeMap) {
    this.recipeMap = recipeMap;
  }

  /**
   * Selects a source building from the list of candidate buildings for the given
   * ingredient,
   * based on the lowest total estimated latency to complete its queued requests.
   *
   * @param sourceList     the list of candidate buildings
   * @param ingredient     the ingredient required
   * @param chooseStandard a map that will be populated with each candidate's name
   *                       and its total
   *                       estimated latency
   * @return the candidate building with the lowest total estimated latency for
   *         producing the ingredient,
   *         or null if no candidate can produce the ingredient
   */
  @Override
  public Building selectSource(Map<Building, GraphPath> sources, String ingredient, Map<String, Integer> chooseStandard) {
    Building bestSource = null;
    int bestTotalTimeEst = Integer.MAX_VALUE;

    // Iterate over candidate sources.
    for (Map.Entry<Building, GraphPath> entry : sources.entrySet()) {
      // if (entry.getValue() == null) continue;
      Building source = entry.getKey();
      if (source.capableOf(ingredient) != null)
        continue;

      int totalTimeEst = 0;
      Queue<Request> sourceRequests = source.getRequests();
      if (sourceRequests.isEmpty()) {
        totalTimeEst = 0;
      } else {
        for (Request req : sourceRequests) {
          UsageInfo usage = new UsageInfo();
          Path path = new Path();
          int timeEst = estimate(req, source, usage, path);
          totalTimeEst += timeEst;
        }
      }
      chooseStandard.put(source.getName(), totalTimeEst);
      if (totalTimeEst < bestTotalTimeEst) {
        bestTotalTimeEst = totalTimeEst;
        bestSource = source;
      }
    }
    return bestSource;
  }

  /**
   * Returns the name of this source selection policy.
   *
   * @return the string "recursiveLat"
   */
  @Override
  public String getSourcePolicyName() {
    String s = "recursiveLat";
    return s;
  }

  /**
   * Recursively estimates the total latency required for the given request to be
   * completed by the specified building.
   *
   * @param request  the Request to estimate latency for
   * @param building the building handling the request
   * @param usage    a UsageInfo object used to track inventory reservations
   *                 across recursive calls
   * @param path     a Path object that tracks the chain of buildings used in the
   *                 estimation process
   * @return the total estimated latency (in time units) required to complete the
   *         request
   */
  public int estimate(Request request, Building building, UsageInfo usage, Path path) {
    int inProgress = usage.checkInProgress(building, request, path);
    if (inProgress >= 0) {
      return inProgress;
    }

    // Storage Handling
    if (building instanceof Storage) {
      Storage storage = (Storage) building;
      String requiredOutput = request.getRecipe().getOutput();
      int neededQty = 1;
      int reservedAmount = usage.getReservedAmount(storage, requiredOutput);
      int effectiveStock = storage.getAmount() - reservedAmount;

      if (effectiveStock >= neededQty) {
        usage.reserveFromStorage(storage, requiredOutput, neededQty, path);
        return 0;
      } else {
        List<Building> candidates = getSourcesThatCanMake(storage, requiredOutput);
        if (candidates.isEmpty()) {
          // No sources available, return a large number for now
          return Integer.MAX_VALUE;
        }
        List<Pair<Building, Integer>> candidateEstimates = new ArrayList<>();
        for (Building src : candidates) {
          Path newPath = path.cloneWith(src);
          UsageInfo newUsage = usage.clonePartial();
          int est = estimate(request, src, newUsage, newPath);
          candidateEstimates.add(new Pair<>(src, est));
        }
        candidateEstimates.sort(Comparator.comparingInt(pair -> pair.second));
        int bestLatency = candidateEstimates.get(0).second;
        // Discard reservations in U for all candidates not chosen.
        for (int i = 1; i < candidateEstimates.size(); i++) {
          usage.discardReservations(candidateEstimates.get(i).first, path);
        }
        return bestLatency;
      }
    }

    int totalTime = request.getRecipe().getLatency();
    for (var entry : request.getRecipe().getIngredients().entrySet()) {
      String ingredint = entry.getKey();
      int neededQty = entry.getValue();

      // Reserve from building's inventory.
      int reserved = usage.reserveInventory(building, ingredint, neededQty, path);
      int remaining = neededQty - reserved;

      // If more units are needed, we produce them using sources.
      while (remaining > 0) {
        List<Building> candidates = getSourcesThatCanMake(building, ingredint);
        if (candidates.isEmpty()) {
          break;
        }
        List<Pair<Building, Integer>> candidateEstimates = new ArrayList<>();

        for (Building src : candidates) {
          Path newPath = path.cloneWith(src);
          UsageInfo newUsage = usage.clonePartial();
          // Create a new request for one unit of the ingredient.
          Recipe ingRecipe = findRecipeFor(ingredint);
          Request subReq = new Request(ingRecipe, src, false);
          int est = estimate(subReq, src, newUsage, newPath);
          candidateEstimates.add(new Pair<>(src, est));
        }

        // Sort candidates by their estimated time in ascending order.
        candidateEstimates.sort(Comparator.comparingInt(pair -> pair.second));

        int A = Math.min(remaining, candidateEstimates.size());
        if (A == 0) {
          break;
        }
        int batchTime = 0;
        for (int i = 0; i < A; i++) {
          batchTime = Math.max(batchTime, candidateEstimates.get(i).second);
        }
        totalTime += batchTime;

        // Discard reservations for candidates that are not chosen.
        for (int i = A; i < candidateEstimates.size(); i++) {
          usage.discardReservations(candidateEstimates.get(i).first, path);
        }

        remaining -= A;
      }
    }
    return totalTime;
  }

  /**
   * Retrieves a list of source buildings from the given building's sources that
   * can produce the specified ingredient.
   *
   * @param building   the building for which to search for sources
   * @param ingredient the ingredient that needs to be produced
   * @return a List of Building objects that are capable of producing the
   *         specified ingredient.
   *         the list may be empty if no such building exists
   */
  private List<Building> getSourcesThatCanMake(Building building, String ingredient) {
    List<Building> result = new ArrayList<>();
    for (Building b : building.getSources()) {
      if (b.capableOf(ingredient) == null)
        result.add(b);
    }
    return result;
  }

  /**
   * Finds and returns the Recipe corresponding to the specified ingredient from
   * the recipe map.
   *
   * @param ingredient the name of the ingredient for which to find a recipe
   * @return the matching Recipe from the recipe map
   * @throws IllegalArgumentException if no recipe is found for the ingredient
   */
  private Recipe findRecipeFor(String ingredient) {
    Recipe recipe = recipeMap.get(ingredient);
    if (recipe == null) {
      throw new IllegalArgumentException("No recipe found for ingredient: " + ingredient);
    }
    return recipe;
  }

  // Helper classes: UsageInfo, Path and Pair.
  /**
   * The UsageInfo class tracks inventory reservations during the recursive
   * latency estimation.
   * 
   * It maintains a mapping between buildings and a sub-map, where the sub-map
   * maps a Path to the
   * number of units reserved from that building's inventory.
   * 
   */
  public static class UsageInfo {
    private Map<Building, Map<Path, Integer>> reservations = new HashMap<>();

    // Reservation tracking
    public int reserveInventory(Building b, String item, int needed, Path p) {
      int available = b.getInventory().getOrDefault(item, 0);
      int reserved = Math.min(needed, available);

      Map<Path, Integer> buildingRes = reservations.computeIfAbsent(b, k -> new HashMap<>());
      buildingRes.merge(p, reserved, Integer::sum);

      return reserved;
    }

    /**
     * Checks if the building is already processing the given request.
     * 
     * If the building's current request matches the given request and is in the
     * WORKING state,
     * the remaining time for that request is returned.
     *
     * @param b the building to check
     * @param r the request to check for
     * @param p the current path (unused in this implementation)
     * @return the remaining time if the request is in progress; otherwise, -1
     */
    public int checkInProgress(Building b, Request r, Path p) {
      if (b.currReq != null && b.currReq.getId() == r.getId() && b.currReq.getState() == RequestState.WORKING) {
        return b.timeLeft;
      }
      return -1;
    }

    /**
     * Discards any reservations made for the specified building along the given
     * path.
     *
     * @param b the building whose reservations are to be discarded
     * @param p the Path that identifies the reservations to remove
     */
    public void discardReservations(Building b, Path p) {
      Map<Path, Integer> buildingRes = reservations.get(b);
      if (buildingRes != null) {
        buildingRes.remove(p);
        if (buildingRes.isEmpty()) {
          reservations.remove(b);
        }
      }
    }

    /**
     * Creates a new, empty UsageInfo instance.
     * 
     * This method is used to create a partial clone for recursive estimation,
     * so that reservations from one branch do not affect another.
     *
     * @return a new UsageInfo with no reservations
     */
    public UsageInfo clonePartial() {
      return new UsageInfo();
    }

    public int getReservedAmount(Building b, String item) {
      int total = 0;
      Map<Path, Integer> buildingRes = reservations.get(b);
      if (buildingRes != null) {
        for (Integer amt : buildingRes.values()) {
          total += amt;
        }
      }
      return total;
    }

    public void reserveFromStorage(Building b, String item, int qty, Path p) {
      Map<Path, Integer> buildingRes = reservations.computeIfAbsent(b, k -> new HashMap<>());
      buildingRes.put(p, qty);
    }
  }

  /**
   * The Path class tracks the sequence of buildings used during the recursive
   * estimation
   * of a request's latency.
   * 
   * Each step in the path is represented as a Pair containing a building and a
   * unique identifier.
   * 
   */
  public static class Path {
    public List<Pair<Building, Integer>> steps = new ArrayList<>();

    public Path cloneWith(Building b) {
      Path newPath = new Path();
      newPath.steps.addAll(this.steps);
      newPath.steps.add(new Pair<>(b, makeUniqueId()));
      return newPath;
    }
  }

  /**
   * A simple generic pair class to hold two related objects.
   *
   * @param <A> the type of the first object
   * @param <B> the type of the second object
   */
  private static class Pair<A, B> {
    public A first;
    public B second;

    /**
     * Constructs a new Pair with the specified values.
     *
     * @param first  the first object
     * @param second the second object
     */
    public Pair(A first, B second) {
      this.first = first;
      this.second = second;
    }
  }

  /**
   * Generates a unique identifier used in Path steps.
   *
   * @return a unique integer identifier
   */
  private static int idCounter = 0;

  private static int makeUniqueId() {
    return ++idCounter;
  }
}
