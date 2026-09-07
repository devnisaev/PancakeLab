package org.pancakelab.logging;

import org.junit.jupiter.api.Test;
import org.pancakelab.domain.OrderEvent;

import java.lang.System.Logger;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SystemShopJournalTest {
    private static final Instant TS = Instant.parse("2026-09-07T12:00:00Z");
    private static final OrderEvent.Created CREATED =
            new OrderEvent.Created(UUID.fromString("11111111-1111-1111-1111-111111111111"), 10, 20, 0, 0);
    private static final OrderEvent.PancakeStarted STARTED =
            new OrderEvent.PancakeStarted(CREATED.orderId(), 10, 20, 0, 1, 1);

    @Test
    void writesInfoLinesWhenLoggerAcceptsTheLevel() {
        CapturingLogger logger = new CapturingLogger(Logger.Level.INFO);
        SystemShopJournal journal = new SystemShopJournal(logger, Clock.fixed(TS, ZoneOffset.UTC));

        journal.record(CREATED);

        assertEquals(1, logger.messages.size());
        assertTrue(logger.messages.get(0).contains("event=Created"));
        assertTrue(logger.messages.get(0).startsWith("ts=2026-09-07T12:00:00Z"));
    }

    @Test
    void skipsDebugEventsWhenLoggerIsInfoOnly() {
        CapturingLogger logger = new CapturingLogger(Logger.Level.INFO);
        SystemShopJournal journal = new SystemShopJournal(logger, Clock.fixed(TS, ZoneOffset.UTC));

        journal.record(STARTED);
        journal.record(new OrderEvent.IngredientAdded(CREATED.orderId(), 10, 20, 0, "Delicious pancake!", 1, 2));

        assertTrue(logger.messages.isEmpty());
    }

    @Test
    void doesNotFailTheCallerWhenLoggerThrows() {
        SystemShopJournal journal = new SystemShopJournal(new ThrowingLogger(), Clock.fixed(TS, ZoneOffset.UTC));
        journal.record(CREATED);
    }

    private static final class CapturingLogger implements Logger {
        private final Level minLevel;
        private final List<String> messages = new ArrayList<>();

        private CapturingLogger(Level minLevel) {
            this.minLevel = minLevel;
        }

        @Override
        public String getName() {
            return "test";
        }

        @Override
        public boolean isLoggable(Level level) {
            return level.getSeverity() >= minLevel.getSeverity();
        }

        @Override
        public void log(Level level, ResourceBundle bundle, String msg, Throwable thrown) {
            if (isLoggable(level)) {
                messages.add(msg);
            }
        }

        @Override
        public void log(Level level, ResourceBundle bundle, String format, Object... params) {
            if (isLoggable(level)) {
                messages.add(format);
            }
        }
    }

    private static final class ThrowingLogger implements Logger {
        @Override
        public String getName() {
            return "throwing";
        }

        @Override
        public boolean isLoggable(Level level) {
            return true;
        }

        @Override
        public void log(Level level, ResourceBundle bundle, String msg, Throwable thrown) {
            throw new IllegalStateException("logger down");
        }

        @Override
        public void log(Level level, ResourceBundle bundle, String format, Object... params) {
            throw new IllegalStateException("logger down");
        }
    }
}
