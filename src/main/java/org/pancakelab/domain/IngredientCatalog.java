package org.pancakelab.domain;

import org.pancakelab.exception.DuplicateIngredientException;
import org.pancakelab.exception.UnknownIngredientException;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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

    private final ConcurrentMap<String, Ingredient> byNormalizedName = new ConcurrentHashMap<>();

    public IngredientCatalog(List<String> ingredients) {
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

    public void add(String name) {
        if (name == null || name.isBlank()) {
            throw new UnknownIngredientException(String.valueOf(name));
        }
        String trimmed = name.trim();
        String key = Ingredient.normalize(trimmed);
        if (byNormalizedName.putIfAbsent(key, new Ingredient(trimmed)) != null) {
            throw new DuplicateIngredientException(trimmed);
        }
    }

    public List<String> names() {
        return byNormalizedName.values().stream()
                .map(Ingredient::name)
                .sorted(Comparator.naturalOrder())
                .toList();
    }
}
