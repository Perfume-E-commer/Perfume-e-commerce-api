package com.Perfume_e_commerce.services;

import com.Perfume_e_commerce.Repositories.PromotionRepository;
import com.Perfume_e_commerce.models.marketing.Promotion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PromotionService {
    @Autowired
    private PromotionRepository promotionRepository;

    public Promotion createPromotion(Promotion promotion) {
        promotion.setCode(promotion.getCode().toUpperCase());
        return promotionRepository.save(promotion);
    }

    public Page<Promotion> getAllPromotions(int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size);
        if (search != null && !search.isEmpty()) {
            return promotionRepository.findByCodeContainingIgnoreCase(search, pageable);
        }
        return promotionRepository.findAll(pageable);
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

        LocalDateTime now = LocalDateTime.now();

        if (promo.getValidFrom() != null && now.isBefore(promo.getValidFrom())) {
            throw new RuntimeException("Promo code is not valid yet");
        }

        if (promo.getValidUntil() != null && now.isAfter(promo.getValidUntil())) {
            throw new RuntimeException("Promo code has expired");
        }

        if (promo.getUsageLimit() != null && promo.getUsageLimit() <= 0) {
            throw new RuntimeException("Promo code usage limit reached");
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
                    if (updatedDetails.getDiscountPercentage() != null && updatedDetails.getDiscountPercentage() > 0) {
                        promo.setDiscountPercentage(updatedDetails.getDiscountPercentage());
                    }
                    if (updatedDetails.getValidFrom() != null) {
                        promo.setValidFrom(updatedDetails.getValidFrom());
                    }
                    if (updatedDetails.getDiscountPercentage() != null && updatedDetails.getDiscountPercentage() > 0) {
                        promo.setDiscountPercentage(updatedDetails.getDiscountPercentage());
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
