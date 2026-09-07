package org.pancakelab.logging;

import org.pancakelab.domain.OrderEvent;

import java.lang.System.Logger.Level;
import java.time.Instant;

public final class Logfmt {
    private Logfmt() {
    }

    public static String line(Instant timestamp, String thread, Level level, OrderEvent event) {
        StringBuilder text = new StringBuilder(160);
        field(text, "ts", timestamp.toString());
        field(text, "level", level.getName());
        field(text, "event", event.getClass().getSimpleName());
        field(text, "orderId", event.orderId().toString());
        field(text, "building", Integer.toString(event.building()));
        field(text, "room", Integer.toString(event.room()));
        field(text, "pancakeCount", Integer.toString(event.pancakeCount()));
        field(text, "version", Integer.toString(event.version()));
        details(text, event);
        field(text, "thread", thread);
        return text.toString();
    }

    private static void details(StringBuilder text, OrderEvent event) {
        if (event instanceof OrderEvent.PancakeStarted started) {
            field(text, "pancakeId", Integer.toString(started.pancakeId()));
        }
        if (event instanceof OrderEvent.IngredientAdded added) {
            field(text, "pancakeId", Integer.toString(added.pancakeId()));
            field(text, "description", added.description());
        }
        if (event instanceof OrderEvent.PancakesRemoved removed) {
            field(text, "removed", Integer.toString(removed.removed()));
            field(text, "description", removed.description());
        }
    }

    private static void field(StringBuilder text, String key, String value) {
        if (!text.isEmpty()) {
            text.append(' ');
        }
        text.append(key).append('=').append(quote(value));
    }

    static String quote(String value) {
        if (value.indexOf(' ') < 0 && value.indexOf('"') < 0) {
            return value;
        }
        return '"' + value.replace("\\", "\\\\").replace("\"", "\\\"") + '"';
    }
}
