package com.smartdealhub.smartdealhub.service;

import com.smartdealhub.smartdealhub.model.StoreHour;
import com.smartdealhub.smartdealhub.repository.StoreHourRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreHourService {

    private final StoreHourRepository storeHourRepository;

    // Get hours by store ID
    public List<StoreHour> getStoreHours(Long storeId) {
        return storeHourRepository.findByStoreStoreId(storeId);
    }

    // Get hours by store name
    public List<StoreHour> getStoreHoursByName(String storeName) {
        return storeHourRepository.findByStoreName(storeName);
    }

    // Add store hour
    public StoreHour addStoreHour(StoreHour storeHour) {
        return storeHourRepository.save(storeHour);
    }

    // Update store hour
    public StoreHour updateStoreHour(StoreHour storeHour) {
        return storeHourRepository.save(storeHour);
    }
}