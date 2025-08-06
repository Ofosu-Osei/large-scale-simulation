package edu.duke.ece651.simulationserver;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a type of factory that can produce a specific set of recipes.
 * A FactoryType is identified by a unique name and maintains a list of
 * Recipe objects that describe the items this factory type can produce.
 */
public class FactoryType {
  private final String name;
  private final List<Recipe> recipes;
  /**
   * Constructs a new FactoryType with the specified name and list of recipes.
   *
   * @param name    the name that identifies this factory type
   * @param recipes a non-empty list of Recipe objects that this factory type can produce
   * @throws IllegalArgumentException if recipes is null or empty
   */
  public FactoryType(String name, List<Recipe> recipes) {
    this.name = name;
    if (recipes == null || recipes.size() == 0) {
      throw new IllegalArgumentException("Invalid input for FactoryType: recipes is null");
    }
    this.recipes = new ArrayList<>(recipes); 
  }

  public String getName() {
    return name;
  }

  public List<Recipe> getRecipes() {
    return recipes; 
  }

 }
