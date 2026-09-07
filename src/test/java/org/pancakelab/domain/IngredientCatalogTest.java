package org.pancakelab.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.pancakelab.exception.DuplicateIngredientException;
import org.pancakelab.exception.UnknownIngredientException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IngredientCatalogTest {
    private final IngredientCatalog catalog = IngredientCatalog.standard();

    @Test
    void requiresKnownIngredientIgnoringCaseAndWhitespace() {
        assertEquals("dark chocolate", catalog.require("  Dark Chocolate ").name());
    }

    @ParameterizedTest
    @ValueSource(strings = {"mustard", "ketchup", "milk"})
    void rejectsUnknownIngredients(String ingredient) {
        UnknownIngredientException exception =
                assertThrows(UnknownIngredientException.class, () -> catalog.require(ingredient));
        assertEquals("Unknown ingredient: " + ingredient, exception.getMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  "})
    void rejectsBlankIngredients(String ingredient) {
        assertThrows(UnknownIngredientException.class, () -> catalog.require(ingredient));
    }

    @Test
    void namesListsAllowlistedIngredientsWithoutMustard() {
        assertTrue(catalog.names().contains("dark chocolate"));
        assertFalse(catalog.names().contains("mustard"));
    }

    @Test
    void addPutsANewIngredientOnTheMenu() {
        catalog.add("caramel");
        assertEquals("caramel", catalog.require(" Caramel ").name());
        assertTrue(catalog.names().contains("caramel"));
    }

    @Test
    void addRejectsDuplicatesAndBlanks() {
        assertThrows(DuplicateIngredientException.class, () -> catalog.add("dark chocolate"));
        assertThrows(UnknownIngredientException.class, () -> catalog.add("  "));
    }
}
