package com.smartdealhub.smartdealhub.model;

import java.io.Serializable;
import java.util.Objects;

public class StoreHourId implements Serializable {

    private Long store;
    private StoreHour.DayOfWeek dayOfWeek;

    public StoreHourId() {}

    public StoreHourId(Long store, StoreHour.DayOfWeek dayOfWeek) {
        this.store = store;
        this.dayOfWeek = dayOfWeek;
    }

    // hashCode & equals
    @Override
    public int hashCode() {
        return Objects.hash(store, dayOfWeek);
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(obj == null || getClass() != obj.getClass()) return false;
        StoreHourId that = (StoreHourId) obj;
        return Objects.equals(store, that.store) && dayOfWeek == that.dayOfWeek;
    }
}