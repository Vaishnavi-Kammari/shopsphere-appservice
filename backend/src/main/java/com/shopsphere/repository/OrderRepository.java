package com.shopsphere.repository;

import com.shopsphere.entity.AppUser;
import com.shopsphere.entity.CustomerOrder;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<CustomerOrder, Long> {
    List<CustomerOrder> findByUserOrderByCreatedAtDesc(AppUser user);
    List<CustomerOrder> findAllByOrderByCreatedAtDesc();
}
