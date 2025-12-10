package com.Perfume_e_commerce.services;

import com.Perfume_e_commerce.Repositories.PromotionRepository;
import com.Perfume_e_commerce.models.marketing.Promotion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class PromotionService {
    @Autowired
    private PromotionRepository promotionRepository;

    public Promotion createPromotion(Promotion promotion) {
        // Ensure code is uppercase
        promotion.setCode(promotion.getCode().toUpperCase());
        return promotionRepository.save(promotion);
    }

    public List<Promotion> getAllPromotions() {
        return promotionRepository.findAll();
    }

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

    public Promotion updatePromotion(String id, Promotion updatedDetails) {
        return promotionRepository.findById(id)
                .map(promo -> {
                    // Update fields (Allow updating Code? Maybe, but be careful)
                    if (updatedDetails.getCode() != null) {
                        promo.setCode(updatedDetails.getCode().toUpperCase());
                    }
                    if (updatedDetails.getDescription() != null) {
                        promo.setDescription(updatedDetails.getDescription());
                    }
                    if (updatedDetails.getDiscountPercent() > 0) {
                        promo.setDiscountPercent(updatedDetails.getDiscountPercent());
                    }
                    if (updatedDetails.getValidUntil() != null) {
                        promo.setValidUntil(updatedDetails.getValidUntil());
                    }
                    // isActive is handled separately or here if you prefer
                    return promotionRepository.save(promo);
                })
                .orElseThrow(() -> new RuntimeException("Promotion not found"));
    }

    public void deletePromotion(String id) {
//        promotionRepository.deleteById(id);

        Promotion promo = promotionRepository.findById(id).orElseThrow();
        promo.setActive(false);
        promotionRepository.save(promo);
    }

    public Promotion toggleActiveStatus(String id) {
        Promotion promo = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promotion not found"));
        promo.setActive(!promo.isActive());
        return promotionRepository.save(promo);
    }
}
