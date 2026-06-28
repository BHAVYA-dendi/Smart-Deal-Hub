package com.smartdealhub.smartdealhub.repository;

import com.smartdealhub.smartdealhub.model.Store;
import com.smartdealhub.smartdealhub.model.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {

    // Find stores by city or state
    List<Store> findByCity(String city);
    List<Store> findByState(String state);

    // Find stores by enum StoreType
    List<Store> findByStoreType(Store.StoreType storeType);

    // Find stores by a list of IDs
    @Query("SELECT s FROM Store s WHERE s.storeId IN :ids")
    List<Store> findAllByIds(List<Integer> ids);

    // Find nearby stores by latitude and longitude
    @Query("SELECT s FROM Store s WHERE " +
            "(6371 * acos(cos(radians(:lat)) * cos(radians(s.latitude)) * cos(radians(s.longitude) - radians(:lon)) + sin(radians(:lat)) * sin(radians(s.latitude)))) <= :radiusKm")
    List<Store> findNearbyStores(double lat, double lon, double radiusKm);

    // Find stores by owner
    List<Store> findByOwnerUserId(Long ownerId);


    // Search by name containing
    @Query("SELECT s FROM Store s WHERE s.name LIKE %:name%")
    List<Store> findByNameContaining(String name);
    List<Store> findByOwner(User owner);

    List<Store> findByStoreType(String storeType);
}