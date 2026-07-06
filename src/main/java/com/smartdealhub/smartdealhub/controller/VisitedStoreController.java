package com.smartdealhub.smartdealhub.controller;

import com.smartdealhub.smartdealhub.model.Store;
import com.smartdealhub.smartdealhub.model.User;
import com.smartdealhub.smartdealhub.model.VisitedStore;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    private User requireCurrentUser(HttpServletRequest request) {
        Object cu = request.getAttribute("currentUser");
        if (cu instanceof User user) return user;
        throw new RuntimeException("Unauthenticated");
    }

    @PostMapping("/add")
    public ResponseEntity<String> addVisit(@RequestBody VisitedStore visit, HttpServletRequest request) {
        User current = requireCurrentUser(request);
        if (visit.getUser() == null || visit.getUser().getUserId() == null) return ResponseEntity.badRequest().build();
        if (current.getRole() != User.Role.ADMIN && !current.getUserId().equals(visit.getUser().getUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(service.addVisit(visit));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<VisitedStore>> getVisitsByUser(@PathVariable Long userId, HttpServletRequest request) {
        User current = requireCurrentUser(request);
        if (current.getRole() != User.Role.ADMIN && !current.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(service.getVisitsByUser(userId));
    }

    @GetMapping("/nearby")
    public List<Store> getNearbyStores(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam double radiusKm) {
        return service.getNearbyStores(lat, lon, radiusKm);
    }
}