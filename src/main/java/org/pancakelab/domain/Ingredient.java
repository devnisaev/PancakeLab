package org.pancakelab.domain;

import java.util.Locale;
import java.util.Objects;

public final class Ingredient {
    private final String name;

    Ingredient(String name) {
        this.name = Objects.requireNonNull(name);
    }

    public String name() {
        return name;
    }

    static String normalize(String raw) {
        return raw.trim().toLowerCase(Locale.ROOT);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ingredient that = (Ingredient) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}
