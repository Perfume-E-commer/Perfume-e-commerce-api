package com.Perfume_e_commerce.Repositories;

import com.Perfume_e_commerce.models.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository {
    Optional<User> findByEmail(String email);
    Boolean existsByEmail(String email);
}
