package edu.duke.ece651.simulationserver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A Recipe represents a production recipe in the simulation.
 * It defines the output product produced by a building (e.g., a factory or mine),
 * the ingredients required (if any), and the latency (number of simulation cycles)
 * required to produce the output.
 * 
 * A recipe is considered a raw resource if it requires no ingredients.
 * 
 */
public class Recipe {
  private final String output;
  private final LinkedHashMap<String, Integer> ingredients;
  private final int latency;
  private final String waste;
  private final int wasteAmount;
  /**
   * Constructs a new Recipe with the specified output, ingredients, and latency.
   * <p>
   * The output name must be non-null and must not contain a single quote character.
   * The latency must be at least 1 and not exceed Integer MAX_VALUE.
   *
   * @param output      the name of the product produced by this recipe
   * @param ingredients a map of ingredient names to the quantities required.
   *                    If null is provided, an empty map is used.
   * @param latency     the number of simulation cycles required to produce the output;
   *                    must be at least 1.
   * @throws IllegalArgumentException if output is null or contains a single quote,
   *                                  or if latency is less than 1 or exceeds Integer.MAX_VALUE
   */
  public Recipe(String output, Map<String, Integer> ingredients, int latency) {
    if (output == null || output.contains("'")) {
      throw new IllegalArgumentException("Invalid output name");
    }
    if (latency < 1 || latency > Integer.MAX_VALUE) {
      throw new IllegalArgumentException("Invalid latency: " + latency);
    }
    this.output = output;
    this.ingredients = ingredients != null ? 
      new LinkedHashMap<>(ingredients) : 
      new LinkedHashMap<>();
    this.latency = latency;
    this.waste = null;
    this.wasteAmount = 0;
  }
  
  public Recipe(String output, String waste, int wasteAmount, Map<String, Integer> ingredients, int latency) {
    if (output == null || output.contains("'")) {
      throw new IllegalArgumentException("Invalid output name");
    }
    if (latency < 1 || latency > Integer.MAX_VALUE) {
      throw new IllegalArgumentException("Invalid latency: " + latency);
    }
    this.output = output;
    this.ingredients = ingredients != null ? 
      new LinkedHashMap<>(ingredients) : 
      new LinkedHashMap<>();
    this.latency = latency;
    this.waste = waste;
    this.wasteAmount = wasteAmount;
  }

  // Getter methods
  /**
   * Returns the output produced by this recipe.
   *
   * @return the output product name
   */
  public String getOutput() {
    return output;
  }

  public String getWaste() {
    return waste;
  }  

  public int getWasteAmount() {
    return wasteAmount;
  }
  
  /**
   * Returns the list of ingredient names in the order they were defined.
   *
   * @return an ArrayList containing the ordered ingredient names
   */
  public List<String> getOrderedIngredients() {
    return new ArrayList<>(ingredients.keySet());
  }

  /**
   * Returns an unmodifiable view of the ingredients required by this recipe.
   *
   * @return an unmodifiable map of ingredients and their required quantities
   */
  public Map<String, Integer> getIngredients() {
    return Collections.unmodifiableMap(ingredients);
  }

  /**
   * Returns the latency (in simulation cycles) required to produce the output of this recipe.
   *
   * @return the production latency
   */
  public int getLatency() {
    return latency;
  }
  /**
   * Determines whether this recipe represents a raw resource.
   *
   * @return true if no ingredients are required, false otherwise
   */
  public boolean isRawResource() {
    return ingredients.isEmpty();
  }

  /**
   * Checks if the recipe's output matches the given name.
   *
   * @param name the name to match against the output
   * @return true if the recipe's output equals the provided name, false otherwise
   */
  boolean match(String name){
    return output.equals(name);
  }
}
