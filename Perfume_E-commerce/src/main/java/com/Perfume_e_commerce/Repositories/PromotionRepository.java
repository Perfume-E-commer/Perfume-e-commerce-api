package com.Perfume_e_commerce.Repositories;

import com.Perfume_e_commerce.models.marketing.Promotion;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PromotionRepository extends MongoRepository<Promotion, String> {
    Optional<Promotion> findByCode(String code);
}
