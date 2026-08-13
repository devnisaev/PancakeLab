package org.pancakelab.model;

import org.pancakelab.exception.UnknownIngredientException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class IngredientCatalog {
    private static final List<String> STANDARD_INGREDIENTS = List.of(
            "dark chocolate",
            "milk chocolate",
            "whipped cream",
            "hazelnuts",
            "strawberries",
            "banana",
            "maple syrup",
            "pecan",
            "coconut",
            "blueberry"
    );

    private final Map<String, Ingredient> byNormalizedName = new LinkedHashMap<>();

    public IngredientCatalog(Iterable<String> ingredients) {
        for (String name : ingredients) {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("Ingredient name must not be blank");
            }
            Ingredient ingredient = new Ingredient(name.trim());
            byNormalizedName.put(Ingredient.normalize(name), ingredient);
        }
        if (byNormalizedName.isEmpty()) {
            throw new IllegalArgumentException("Ingredient catalog must not be empty");
        }
    }

    public static IngredientCatalog standard() {
        return new IngredientCatalog(STANDARD_INGREDIENTS);
    }

    public Ingredient require(String name) {
        if (name == null || name.isBlank()) {
            throw new UnknownIngredientException(String.valueOf(name));
        }
        Ingredient ingredient = byNormalizedName.get(Ingredient.normalize(name));
        if (ingredient == null) {
            throw new UnknownIngredientException(name.trim());
        }
        return ingredient;
    }
}
