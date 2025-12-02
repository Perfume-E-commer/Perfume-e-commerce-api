package com.Perfume_e_commerce.services;

import com.Perfume_e_commerce.Repositories.PromotionRepository;
import com.Perfume_e_commerce.models.Promotion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class PromotionService {
    @Autowired
    private PromotionRepository promotionRepository;


    // Admin: Create a new promotion
    public Promotion createPromotion(Promotion promotion) {
        // Ensure code is uppercase
        promotion.setCode(promotion.getCode().toUpperCase());
        return promotionRepository.save(promotion);
    }

    // Admin: Get all
    public List<Promotion> getAllPromotions() {
        return promotionRepository.findAll();
    }

    // User: Validate code
    public Promotion validatePromotion(String code) {
        Optional<Promotion> promoOpt = promotionRepository.findByCode(code.toUpperCase());

        if (promoOpt.isEmpty()) {
            throw new RuntimeException("Invalid promo code");
        }

        Promotion promo = promoOpt.get();

        if (!promo.isActive()) {
            throw new RuntimeException("Promo code is inactive");
        }

        if (promo.getValidUntil().before(new Date())) {
            throw new RuntimeException("Promo code has expired");
        }

        return promo;
    }
}
