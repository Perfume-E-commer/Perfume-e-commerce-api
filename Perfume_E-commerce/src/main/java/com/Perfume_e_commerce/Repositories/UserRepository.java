package com.Perfume_e_commerce.Repositories;

import com.Perfume_e_commerce.models.user.User;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, ObjectId> {
    Optional<User> findByEmail(String email);
    Boolean existsByEmail(String email);
    List<User> findByRole(String role);

    @Query("{ '$or': [ " +
            "{ 'firstName': { '$regex': ?0, '$options': 'i' } }, " +
            "{ 'lastName': { '$regex': ?0, '$options': 'i' } }, " +
            "{ 'email': { '$regex': ?0, '$options': 'i' } } " +
            "] }")
    Page<User> searchUsers(String keyword, Pageable pageable);
}
