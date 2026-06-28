package com.smartdealhub.smartdealhub.model;

import jakarta.persistence.*;
import java.time.LocalTime;

@Entity
@Table(name = "store_hours")
@IdClass(StoreHourId.class)
public class StoreHour {

    @Id
    @ManyToOne
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private DayOfWeek dayOfWeek;

    @Column(name = "open_time")
    private LocalTime openTime;

    @Column(name = "close_time")
    private LocalTime closeTime;

    public StoreHour() {}

    public StoreHour(Store store, DayOfWeek dayOfWeek, LocalTime openTime, LocalTime closeTime) {
        this.store = store;
        this.dayOfWeek = dayOfWeek;
        this.openTime = openTime;
        this.closeTime = closeTime;
    }

    // Getters & Setters
    public Store getStore() { return store; }
    public void setStore(Store store) { this.store = store; }
    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(DayOfWeek dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    public LocalTime getOpenTime() { return openTime; }
    public void setOpenTime(LocalTime openTime) { this.openTime = openTime; }
    public LocalTime getCloseTime() { return closeTime; }
    public void setCloseTime(LocalTime closeTime) { this.closeTime = closeTime; }

    // Enum for day of week
    public enum DayOfWeek {
        Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday
    }
}