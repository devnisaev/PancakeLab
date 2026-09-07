package org.pancakelab.exception;

public class UnknownIngredientException extends ShopException {
    public UnknownIngredientException(String ingredient) {
        super("Unknown ingredient: " + ingredient);
    }
}
