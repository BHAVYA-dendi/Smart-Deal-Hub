package com.smartdealhub.smartdealhub.service;

import com.smartdealhub.smartdealhub.model.VisitedStore;
import com.smartdealhub.smartdealhub.model.Store;
import com.smartdealhub.smartdealhub.model.User;
import com.smartdealhub.smartdealhub.repository.VisitedStoreRepository;
import com.smartdealhub.smartdealhub.repository.StoreRepository;
import com.smartdealhub.smartdealhub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VisitedStoreService {

    private final VisitedStoreRepository visitedStoreRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;

    // ================= ADD VISIT =================
    public String addVisit(VisitedStore visit) {
        visitedStoreRepository.save(visit);
        return "Visit recorded";
    }

    public void addVisit(Long userId, Long storeId) {
        VisitedStore visit = new VisitedStore();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found with ID: " + storeId));

        visit.setUser(user);
        visit.setStore(store);
        visitedStoreRepository.save(visit);
    }

    // ================= GET VISITS =================
    public List<VisitedStore> getVisitsByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        return visitedStoreRepository.findByUser(user);
    }

    public List<Store> getNearbyStores(double lat, double lon, double radiusKm) {
        return storeRepository.findNearbyStores(lat, lon, radiusKm);
    }

    public List<Store> getVisitedStoresByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        return visitedStoreRepository.findByUser(user).stream()
                .map(VisitedStore::getStore)
                .collect(Collectors.toList());
    }

    public List<User> getVisitorsForOwnerStores(Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("Owner not found with ID: " + ownerId));

        List<Store> stores = storeRepository.findByOwner(owner);

        return stores.stream()
                .flatMap(store -> visitedStoreRepository.findByStore(store).stream())
                .map(VisitedStore::getUser)
                .collect(Collectors.toList());
    }
}