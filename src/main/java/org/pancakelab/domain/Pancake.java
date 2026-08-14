package org.pancakelab.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class Pancake {
    private final int id;
    private final List<Ingredient> ingredients = new ArrayList<>();

    Pancake(int id) {
        this.id = id;
    }

    public int id() {
        return id;
    }

    public void addIngredient(Ingredient ingredient) {
        ingredients.add(Objects.requireNonNull(ingredient, "ingredient"));
    }

    public boolean isEmpty() {
        return ingredients.isEmpty();
    }

    public List<String> ingredientNames() {
        return ingredients.stream().map(Ingredient::name).toList();
    }

    public String description() {
        if (ingredients.isEmpty()) {
            return "Delicious pancake!";
        }
        return "Delicious pancake with %s!".formatted(String.join(", ", ingredientNames()));
    }
}
