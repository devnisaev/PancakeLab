package org.pancakelab.exception;

public class PancakeNotFoundException extends ShopException {
    public PancakeNotFoundException(int pancakeId) {
        super("Pancake %d does not exist".formatted(pancakeId));
    }
}
