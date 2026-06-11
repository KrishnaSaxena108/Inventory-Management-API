package com.inventory.event;

public final class EventAction {

    public static final String CREATE = "CREATE";
    public static final String UPDATE = "UPDATE";
    public static final String DELETE = "DELETE";
    public static final String STOCK_ADD = "STOCK_ADD";
    public static final String STOCK_REDUCE = "STOCK_REDUCE";
    public static final String STOCK_RECEIVE = "STOCK_RECEIVE";

    private EventAction() {
    }
}
