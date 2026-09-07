package org.pancakelab.logging;

import org.pancakelab.domain.OrderEvent;

import java.util.Objects;

public enum DiscardingShopJournal implements ShopJournal {
    INSTANCE;

    @Override
    public void record(OrderEvent event) {
        Objects.requireNonNull(event, "event");
    }
}
