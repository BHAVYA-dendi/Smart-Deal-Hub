package com.smartdealhub.smartdealhub.repository;

import com.smartdealhub.smartdealhub.model.StoreHour;
import com.smartdealhub.smartdealhub.model.StoreHourId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoreHourRepository extends JpaRepository<StoreHour, StoreHourId> {

    List<StoreHour> findByStoreStoreId(Long storeId);

    List<StoreHour> findByStoreName(String storeName);
}