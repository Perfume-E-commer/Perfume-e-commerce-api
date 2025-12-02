package com.Perfume_e_commerce.Repositories;

import com.Perfume_e_commerce.models.user.VerificationCode;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface VerificationCodeRepository extends MongoRepository<VerificationCode, String> {
    Optional<VerificationCode> findByEmail(String email);
    void deleteByEmail(String email);
}
