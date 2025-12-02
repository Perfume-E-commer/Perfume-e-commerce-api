package com.Perfume_e_commerce.jpa;

import com.Perfume_e_commerce.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductJpaRepository extends JpaRepository<ProductEntity, String> {
}
