package com.Perfume_e_commerce.jpa;

import com.Perfume_e_commerce.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserJpaRepository extends JpaRepository<UserEntity, String> {
}
