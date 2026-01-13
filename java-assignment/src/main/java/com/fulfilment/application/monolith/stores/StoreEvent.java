package com.fulfilment.application.monolith.stores;

public class StoreEvent {
    private final Store store;
    private final StoreEventType type;

    public StoreEvent(Store store, StoreEventType type) {
        this.store = store;
        this.type = type;
    }

    public Store getStore() {
        return store;
    }

    public StoreEventType getType() {
        return type;
    }
}
