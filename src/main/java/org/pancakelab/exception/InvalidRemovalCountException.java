package org.pancakelab.exception;

public class InvalidRemovalCountException extends ShopException {
    public InvalidRemovalCountException() {
        super("Count must be positive");
    }
}
