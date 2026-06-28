package com.smartdealhub.smartdealhub.repository;

import com.smartdealhub.smartdealhub.model.StoreHour;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoreHourRepository extends JpaRepository<StoreHour, Integer> {

    List<StoreHour> findByStoreStoreId(int storeId);

    List<StoreHour> findByStoreName(String storeName);
}