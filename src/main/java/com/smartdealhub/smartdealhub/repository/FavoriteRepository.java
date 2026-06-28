package com.smartdealhub.smartdealhub.repository;

import com.smartdealhub.smartdealhub.model.Favorite;
import com.smartdealhub.smartdealhub.model.Product;
import com.smartdealhub.smartdealhub.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    Favorite findByUserAndProduct(User user, Product product);

    void deleteByUser(User user);

    void deleteByProduct(Product product);

    List<Favorite> findAllByUser(User user);

    List<Favorite> findByUserAndProduct_StoreStoreId(User user, Long storeId);

    // ✅ Corrected: use actual field names in User and Product entities
    void deleteByUser_UserId(Long userId);

    Optional<Favorite> findByUser_UserIdAndProduct_Id(Long userId, Long productId);
}
