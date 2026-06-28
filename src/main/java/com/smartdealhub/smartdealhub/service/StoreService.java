package com.smartdealhub.smartdealhub.service;

import com.smartdealhub.smartdealhub.model.Store;
import com.smartdealhub.smartdealhub.model.User;
import com.smartdealhub.smartdealhub.repository.StoreRepository;
import com.smartdealhub.smartdealhub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;
    private final UserRepository userRepository;

    // ================= CREATE =================
    public Store createStore(Store store, Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("Owner not found with ID: " + ownerId));
        store.setOwner(owner);
        return storeRepository.save(store);
    }
    
    /**
     * Saves a store without changing the owner
     * @param store The store to save
     * @return The saved store
     */
    public Store saveStore(Store store) {
        if (store == null) {
            throw new IllegalArgumentException("Store cannot be null");
        }
        return storeRepository.save(store);
    }

    // ================= UPDATE =================
    public Store updateStore(Long storeId, Store updatedStore) {
        Store existing = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found with ID: " + storeId));

        existing.setName(updatedStore.getName());
        existing.setStoreType(updatedStore.getStoreType());
        existing.setCity(updatedStore.getCity());
        existing.setState(updatedStore.getState());
        existing.setLatitude(updatedStore.getLatitude());
        existing.setLongitude(updatedStore.getLongitude());

        return storeRepository.save(existing);
    }

    // ================= DELETE =================
    public void deleteStore(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found with ID: " + storeId));
        storeRepository.delete(store);
    }

    // ================= GET =================
    public Store getStoreById(Long storeId) {
        Store s = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found with ID: " + storeId));
        return trimStore(s);
    }

    public List<Store> getAllStores() {
        return storeRepository.findAll().stream()
                .map(this::trimStore)
                .toList();
    }

    // Lean version to avoid recursive owner->stores loops in JSON
    public List<Store> getAllStoresLean() {
        return storeRepository.findAll().stream()
                .map(this::trimStore)
                .toList();
    }

    private Store trimStore(Store s){
        if(s == null) return null;
        try{
            if(s.getOwner()!=null){
                // Break potential recursion and sensitive data
                s.getOwner().setStores(null);
                s.getOwner().setPassword(null);
            }
            // Also remove heavy backrefs/collections if present
            try { s.setProducts(null); } catch(Exception ignored){}
            try { s.setStoreHours(null); } catch(Exception ignored){}
        }catch(Exception ignored){}
        return s;
    }

    public List<Store> getStoresByCity(String city) {
        return storeRepository.findByCity(city).stream().map(this::trimStore).toList();
    }

    public List<Store> getStoresByState(String state) {
        return storeRepository.findByState(state).stream().map(this::trimStore).toList();
    }

    public List<Store> getStoresByType(Store.StoreType storeType) {
        return storeRepository.findByStoreType(storeType).stream().map(this::trimStore).toList();
    }

    public List<Store> getStoresByType(String type) {
        try {
            Store.StoreType st = Store.StoreType.valueOf(type);
            return getStoresByType(st);
        } catch(Exception e){
            return List.of();
        }
    }

    public List<Store> getStoresByOwner(Long ownerId) {
        return storeRepository.findByOwnerUserId(ownerId).stream().map(this::trimStore).toList();
    }

    public List<Store> searchStoresByName(String name) {
        return storeRepository.findByNameContaining(name).stream().map(this::trimStore).toList();
    }

    // ================= ADVANCED =================
    public List<Store> getStoresByIds(List<Integer> ids) {
        return storeRepository.findAllByIds(ids).stream().map(this::trimStore).toList();
    }

    public List<Store> findNearbyStores(double lat, double lon, double radiusKm) {
        return storeRepository.findNearbyStores(lat, lon, radiusKm).stream()
                .map(this::trimStore)
                .toList();
    }

    // ================= OWNER-SCOPED HELPERS =================
    public Store getOwnerSingleStore(User owner) {
        List<Store> stores = storeRepository.findByOwner(owner);
        if (stores == null || stores.isEmpty()) {
            return null;
        }
        // If multiple exist, pick the first as active scope
        return stores.get(0);
    }

    public Store upsertOwnerStore(User owner, Store payload) {
        if (owner == null) throw new RuntimeException("Missing owner");
        Store existing = getOwnerSingleStore(owner);
        if (existing == null) {
            payload.setOwner(owner);
            return storeRepository.save(payload);
        }
        if (payload.getName() != null) existing.setName(payload.getName());
        if (payload.getStoreType() != null) existing.setStoreType(payload.getStoreType());
        if (payload.getCity() != null) existing.setCity(payload.getCity());
        if (payload.getState() != null) existing.setState(payload.getState());
        if (payload.getLatitude() != null) existing.setLatitude(payload.getLatitude());
        if (payload.getLongitude() != null) existing.setLongitude(payload.getLongitude());
        return storeRepository.save(existing);
    }
}