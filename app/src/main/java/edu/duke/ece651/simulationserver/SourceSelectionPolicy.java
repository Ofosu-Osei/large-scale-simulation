package edu.duke.ece651.simulationserver;

import java.util.Map;

/**
 * This is an interface for selecting an appropriate source Building to supply a needed ingredient.
 */
public interface SourceSelectionPolicy {
  /**
     * Selects a source Building from the provided Factory's sources that can supply the given ingredient.
     *
     * @param sources the list of source buildings from the factory
     * @param ingredient the name of the ingredient required
     * @return the selected Building that can supply the ingredient, or null if none is available
     */
  Building selectSource(Map<Building, GraphPath> sources, String ingredient,Map<String,Integer> chooseStandard);
    String getSourcePolicyName();
}
