package edu.duke.ece651.simulationserver;

import java.util.Collections;
import java.util.List;

/**
 * Represents a Mine in the simulation.
 * 
 * A Mine is a special type of Building that produces a raw resource
 * without requiring any input ingredients. The resource produced by a mine is an
 * an output from the recipe class, and specifies what this mine creates.
 */
public class Mine extends Building {
  private final Recipe mineRecipe; 
  
  /**
   * Constructs a new Mine with the given name, source list, and recipe.
   * The sources list is generally empty for a Mine, as it does not require input ingredients.
   *
   * @param name      the unique name of this mine
   * @param sources   the list of source buildings (typically empty for mines)
   * @param mineRecipe the Recipe that defines the raw resource produced by this mine
   */
  public Mine(String name, List<Building> sources, Recipe mineRecipe){
    super(name, sources);
    this.mineRecipe = mineRecipe;
  }

  public Mine(String name, List<Building> sources, Recipe mineRecipe, Coordinate c){
    super(name, sources, c);
    this.mineRecipe = mineRecipe;
  }

  
  /**
   * Retrieves the recipe associated with this mine.
   *
   * @return the Recipe for this mine
   */
  public Recipe getRecipe() {
    return mineRecipe;
  }
  
  /**
   * Retrieves the list of recipes available for this mine.
   *
   * Since a mine produces a single raw resource, this method returns a singleton list
   * containing only itsmineRecipe}.
   *
   * @return an unmodifiable list containing the mine's recipe
   */
  @Override
  protected List<Recipe> getRecipes() {
    return Collections.singletonList(mineRecipe);
  }
 
  /**
   * Determines whether this mine can produce the specified ingredient.
   *
   * @param ingredient the name of the ingredient to check
   * @return true if the mine produces the given ingredient or false otherwise
   */
  @Override
  public boolean mayProduce(String product) {
    return this.mineRecipe.getOutput().equals(product);
  }

  @Override
  public String capableOf(String product) {
    if (removeMark) {
      return "being removed";
    }
    if (mayProduce(product)) {
      return null;
    }
    else {
      return "mine '" + name + "' cannot produce '" + product + "'";
    }
  }

}
