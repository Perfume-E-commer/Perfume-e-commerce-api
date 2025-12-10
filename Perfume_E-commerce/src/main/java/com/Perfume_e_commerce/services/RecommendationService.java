package com.Perfume_e_commerce.services;

import com.Perfume_e_commerce.Repositories.ProductRepository;
import com.Perfume_e_commerce.Repositories.UserRepository;
import com.Perfume_e_commerce.models.product.Product;
import com.Perfume_e_commerce.models.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecommendationService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    public void recordView(String userId, String productId) {
        User user = userRepository.findById(new org.bson.types.ObjectId(userId))
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<String> views = user.getViewedProductIds();

        views.remove(productId);

        views.add(productId);

        if (views.size() > 20) {
            views.remove(0);
        }

        user.setViewedProductIds(views);
        userRepository.save(user);
    }

    public List<Product> getRecommendations(String userId) {
        User user = userRepository.findById(new org.bson.types.ObjectId(userId))
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<String> history = user.getViewedProductIds();

        if (history.isEmpty()) {
            return productRepository.findByIsActiveTrue().stream()
                    .limit(5)
                    .collect(Collectors.toList());
        }

        String lastViewedId = history.get(history.size() - 1);
        Product lastProduct = productRepository.findById(lastViewedId).orElse(null);

        if (lastProduct == null) return Collections.emptyList();

        List<Product> recommendations = productRepository.findByCategoryAndIsActiveTrue(lastProduct.getCategory());

        return recommendations.stream()
                .filter(p -> !p.getId().equals(lastViewedId))
                .limit(6)
                .collect(Collectors.toList());
    }
}
