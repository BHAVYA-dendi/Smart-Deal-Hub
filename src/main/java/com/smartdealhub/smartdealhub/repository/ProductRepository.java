package com.smartdealhub.smartdealhub.repository;

import com.smartdealhub.smartdealhub.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.store")
    List<Product> findAllWithStore();

    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.store WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Product> searchByNameWithStore(@Param("query") String query);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.store WHERE p.id = :id")
    java.util.Optional<Product> findByIdWithStore(@Param("id") Long id);

    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.store WHERE p.category = :category")
    List<Product> findByCategoryWithStore(@Param("category") String category);

    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.store WHERE p.brand = :brand")
    List<Product> findByBrandWithStore(@Param("brand") String brand);

    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.store WHERE p.category = :category AND p.brand = :brand")
    List<Product> findByCategoryAndBrandWithStore(@Param("category") String category, @Param("brand") String brand);
}