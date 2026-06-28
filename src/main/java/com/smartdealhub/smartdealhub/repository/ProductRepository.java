package com.smartdealhub.smartdealhub.repository;

import com.smartdealhub.smartdealhub.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByNameContainingIgnoreCase(String name);
    List<Product> findByCategoryAndBrand(String category, String brand);
    List<Product> findByTagsContainingIgnoreCase(String tag);

    List<Product> findByStoreStoreId(Long storeId);

    List<Product> findByStoreStoreIdAndCategory(Long storeId, String category);

    List<Product> findByCategory(String category);

    List<Product> findByBrand(String brand);

    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE %:query%")
    List<Product> searchByName(String query);
}