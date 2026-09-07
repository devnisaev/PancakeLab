package org.pancakelab.exception;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShopExceptionTest {
    @Test
    void shopRulesAreBusinessExceptionsNotIllegalArguments() {
        ShopException mustard = new UnknownIngredientException("mustard");
        ShopException missingOrder = new OrderNotFoundException(UUID.randomUUID());
        ShopException missingPancake = new PancakeNotFoundException(99);
        ShopException badCount = new InvalidRemovalCountException();

        assertInstanceOf(RuntimeException.class, mustard);
        assertFalse(IllegalArgumentException.class.isInstance(mustard));
        assertFalse(IllegalArgumentException.class.isInstance(missingOrder));
        assertTrue(missingPancake instanceof ShopException);
        assertTrue(badCount instanceof ShopException);
        assertTrue(new IllegalOrderStateException("empty") instanceof ShopException);
        assertTrue(new InvalidLocationException("gone") instanceof ShopException);
        assertTrue(new DuplicateIngredientException("banana") instanceof ShopException);
    }
}
