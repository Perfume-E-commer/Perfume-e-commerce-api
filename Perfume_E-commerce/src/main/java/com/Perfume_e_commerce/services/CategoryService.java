package com.Perfume_e_commerce.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.Perfume_e_commerce.Repositories.CategoryRepository;
import com.Perfume_e_commerce.models.product.Category;

@Service
public class CategoryService {
    @Autowired
    private CategoryRepository repository;

    public List<Category> getAllCategories() {
        return repository.findAll();
    }

    public Category createCategory(Category category) {
        return repository.save(category);
    }

    public void deleteCategory(String id) {
        repository.deleteById(id);
    }
}
