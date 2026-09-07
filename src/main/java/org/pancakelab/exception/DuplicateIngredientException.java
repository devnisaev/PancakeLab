package org.pancakelab.exception;

public class DuplicateIngredientException extends ShopException {
    public DuplicateIngredientException(String ingredient) {
        super("Ingredient already on the menu: " + ingredient);
    }
}
