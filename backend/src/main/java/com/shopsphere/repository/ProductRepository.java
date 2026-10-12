package com.shopsphere.repository;

import com.shopsphere.entity.Product;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("select p from Product p where lower(p.name) like lower(concat('%', :q, '%')) "
         + "or lower(p.description) like lower(concat('%', :q, '%')) order by p.id")
    List<Product> search(@Param("q") String q);

    @Query("select p from Product p where p.category.id = :categoryId and "
         + "(lower(p.name) like lower(concat('%', :q, '%')) "
         + "or lower(p.description) like lower(concat('%', :q, '%'))) order by p.id")
    List<Product> searchInCategory(@Param("q") String q, @Param("categoryId") Long categoryId);
}
