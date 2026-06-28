package com.smartdealhub.smartdealhub.controller;

import com.smartdealhub.smartdealhub.model.Store;
import com.smartdealhub.smartdealhub.model.VisitedStore;
import com.smartdealhub.smartdealhub.service.VisitedStoreService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/visited-store")
public class VisitedStoreController {

    private final VisitedStoreService service;

    public VisitedStoreController(VisitedStoreService service) {
        this.service = service;
    }

    @PostMapping("/add")
    public String addVisit(@RequestBody VisitedStore visit) {
        return service.addVisit(visit);
    }

    @GetMapping("/user/{userId}")
    public List<VisitedStore> getVisitsByUser(@PathVariable Long userId) {
        return service.getVisitsByUser(userId);
    }

    @GetMapping("/nearby")
    public List<Store> getNearbyStores(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam double radiusKm) {
        return service.getNearbyStores(lat, lon, radiusKm);
    }
}