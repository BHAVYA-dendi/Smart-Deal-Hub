package com.smartdealhub.smartdealhub.repository;

import com.smartdealhub.smartdealhub.model.VisitedStore;
import com.smartdealhub.smartdealhub.model.Store;
import com.smartdealhub.smartdealhub.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VisitedStoreRepository extends JpaRepository<VisitedStore, Long> {

    // Find all visits by a specific user
    List<VisitedStore> findByUser(User user);

    // Find all visits for a specific store
    List<VisitedStore> findByStore(Store store);
}