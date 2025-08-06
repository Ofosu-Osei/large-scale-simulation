package edu.duke.ece651.simulationserver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class RecipeTest {
    // Test valid constructor parameters and getter methods
    @Test
    public void testValidConstructorAndGetters() {
        LinkedHashMap<String, Integer> ingredients = new LinkedHashMap<>();
        ingredients.put("wood", 2);
        ingredients.put("steel", 3);
        
        Recipe recipe = new Recipe("door", ingredients, 10);
        
        // Verify output name
        assertEquals("door", recipe.getOutput());
        
        // Verify ingredient order preservation
        List<String> ordered = recipe.getOrderedIngredients();
        assertEquals("wood", ordered.get(0));
        assertEquals("steel", ordered.get(1));
        
        // Verify unmodifiable ingredients map
        Map<String, Integer> map = recipe.getIngredients();
        assertEquals(2, map.get("wood"));
        assertThrows(UnsupportedOperationException.class, () -> map.put("glass", 1));
        
        // Verify latency value
        assertEquals(10, recipe.getLatency());
    }

    // Test null output name validation
    @Test
    public void testNullOutputName() {
        assertThrows(IllegalArgumentException.class, 
            () -> new Recipe(null, new LinkedHashMap<>(), 1),
            "Should throw for null output name"
        );
    }

    // Test apostrophe in output name validation
    @Test
    public void testInvalidOutputNameWithApostrophe() {
        assertThrows(IllegalArgumentException.class, 
            () -> new Recipe("do'or", new LinkedHashMap<>(), 1)
        );
    }

    // Test latency lower boundary validation
    @Test
    public void testLatencyBelowOne() {
        assertThrows(IllegalArgumentException.class, 
            () -> new Recipe("door", new LinkedHashMap<>(), 0)
        );
    }

    // Test latency upper boundary validation (using long cast for overflow case)
    @Test
    public void testLatencyExceedsMaxValue() {
        assertThrows(IllegalArgumentException.class, 
            () -> new Recipe("door", new LinkedHashMap<>(), (int) (Integer.MAX_VALUE + 1L))
        );
    }

    // Test null ingredients parameter handling
    @Test
    public void testNullIngredientsConvertToEmptyMap() {
        Recipe recipe = new Recipe("iron", null, 1);
        assertTrue(recipe.getIngredients().isEmpty());
    }

    // Test raw resource detection with empty ingredients
    @Test
    public void testIsRawResourceWithEmptyIngredients() {
        Recipe recipe = new Recipe("iron", new LinkedHashMap<>(), 1);
        assertTrue(recipe.isRawResource());
    }

    // Test non-raw resource detection
    @Test
    public void testIsNotRawResourceWithIngredients() {
        LinkedHashMap<String, Integer> ingredients = new LinkedHashMap<>();
        ingredients.put("ore", 1);
        Recipe recipe = new Recipe("steel", ingredients, 5);
        assertFalse(recipe.isRawResource());
    }

    // Test ingredient insertion order preservation
    @Test
    public void testIngredientOrderConsistency() {
        LinkedHashMap<String, Integer> ingredients = new LinkedHashMap<>();
        ingredients.put("A", 1);
        ingredients.put("B", 2);
        ingredients.put("C", 3);
        
        Recipe recipe = new Recipe("test", ingredients, 1);
        List<String> ordered = recipe.getOrderedIngredients();
        assertEquals("A", ordered.get(0));
        assertEquals("B", ordered.get(1));
        assertEquals("C", ordered.get(2));
    }
}
