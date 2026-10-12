package com.shopsphere.repository;

import com.shopsphere.entity.AppUser;
import com.shopsphere.entity.CartItem;
import com.shopsphere.entity.Product;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUserOrderById(AppUser user);
    Optional<CartItem> findByUserAndProduct(AppUser user, Product product);
    void deleteByProduct_Id(Long productId);
}
