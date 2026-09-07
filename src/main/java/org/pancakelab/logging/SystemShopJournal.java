package org.pancakelab.logging;

import org.pancakelab.domain.OrderEvent;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.time.Clock;
import java.util.Objects;

public final class SystemShopJournal implements ShopJournal {
    private static final String LOGGER_NAME = "org.pancakelab.order";

    private final Logger logger;
    private final Clock clock;

    public SystemShopJournal() {
        this(System.getLogger(LOGGER_NAME), Clock.systemUTC());
    }

    SystemShopJournal(Logger logger, Clock clock) {
        this.logger = Objects.requireNonNull(logger, "logger");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    @Override
    public void record(OrderEvent event) {
        Objects.requireNonNull(event, "event");
        Level level = levelOf(event);
        if (!logger.isLoggable(level)) {
            return;
        }
        try {
            logger.log(level, Logfmt.line(clock.instant(), Thread.currentThread().getName(), level, event));
        } catch (RuntimeException ignored) {
            System.err.println("Failed to journal " + event.getClass().getSimpleName());
        }
    }

    static Level levelOf(OrderEvent event) {
        if (event instanceof OrderEvent.PancakeStarted || event instanceof OrderEvent.IngredientAdded) {
            return Level.DEBUG;
        }
        return Level.INFO;
    }
}
