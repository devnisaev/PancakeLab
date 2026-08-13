package org.pancakelab.exception;

public class UnknownIngredientException extends IllegalArgumentException {
    public UnknownIngredientException(String ingredient) {
        super("Unknown ingredient: " + ingredient);
    }
}
