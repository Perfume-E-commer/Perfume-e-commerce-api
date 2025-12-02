package com.Perfume_e_commerce.services;

import com.Perfume_e_commerce.Repositories.ProductRepository;
import com.Perfume_e_commerce.dto.ProductDTO;
import com.Perfume_e_commerce.dto.ProductFilterDTO;
import com.Perfume_e_commerce.models.Product;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;

    public List<Product> findByCategoryAndIsActiveTrue(String category) {
        return productRepository.findByCategoryAndIsActiveTrue(category);
    }

    public Optional<Product> findById(String id) {
        return productRepository.findById(id);
    }

    public List<Product> getAllActiveProducts() {
        return productRepository.findByIsActiveTrue();
    }

    public Optional<Product> getProductById(String id) {
        try {
            ObjectId objectId = new ObjectId(id);
            return productRepository.findByIdAndIsActiveTrue(objectId);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    public void deleteProduct(String id) {
        productRepository.deleteById(new ObjectId(id));
    }

    public Product updateProduct(String id, Product updatedDetails) {
        ObjectId objectId = new ObjectId(id);
        return productRepository.findById(objectId)
                .map(existingProduct -> {
                    existingProduct.setName(updatedDetails.getName());
                    existingProduct.setBrand(updatedDetails.getBrand());
                    existingProduct.setDescription(updatedDetails.getDescription());
                    existingProduct.setPrice(updatedDetails.getPrice());
                    existingProduct.setStock(updatedDetails.getStock());
                    existingProduct.setCategory(updatedDetails.getCategory());
                    existingProduct.setImageUrl(updatedDetails.getImageUrl());
                    // We generally don't update 'createdAt' or 'id'
                    return productRepository.save(existingProduct);
                })
                .orElseThrow(() -> new RuntimeException("Product not found with id " + id));
    }

    public Product patchProduct(String id, Map<String, Object> updates) {
        ObjectId objectId = new ObjectId(id);
        return productRepository.findById(objectId)
                .map(product -> {
                    updates.forEach((key, value) -> {
                        switch (key) {
                            case "name":
                                product.setName((String) value);
                                break;
                            case "brand":
                                product.setBrand((String) value);
                                break;
                            case "description":
                                product.setDescription((String) value);
                                break;
                            case "price":
                                // JSON numbers often come as Integer/Double, so we convert safely
                                product.setPrice(new java.math.BigDecimal(String.valueOf(value)));
                                break;
                            case "stock":
                                product.setStock((Integer) value);
                                break;
                            case "category":
                                product.setCategory((String) value);
                                break;
                            case "imageUrl":
                                product.setImageUrl((String) value);
                                break;
                            case "isActive":
                                product.setActive((Boolean) value);
                                break;
                            // We ignore 'id' or 'createdAt' to prevent hacking
                        }
                    });
                    return productRepository.save(product);
                })
                .orElseThrow(() -> new RuntimeException("Product not found with id " + id));
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Page<ProductDTO> getFilteredProducts(ProductFilterDTO filter) {
        List<Product> products = productRepository.findAll();

        // Apply filters
        List<Product> filteredProducts = products.stream()
                .filter(p -> filter.getSearch() == null ||
                        p.getName().toLowerCase().contains(filter.getSearch().toLowerCase()) ||
                        p.getBrand().toLowerCase().contains(filter.getSearch().toLowerCase()))
                .filter(p -> filter.getCategory() == null ||
                        p.getCategory().equalsIgnoreCase(filter.getCategory()))
                .filter(p -> filter.getGender() == null ||
                        (p.getGender() != null && p.getGender().equalsIgnoreCase(filter.getGender())))
                .filter(p -> filter.getFeatured() == null ||
                        (filter.getFeatured() && p.getIsFeatured()))
                .filter(p -> filter.getOnSale() == null ||
                        (filter.getOnSale() && p.getIsOnSale()))
                .filter(p -> filter.getOutOfStock() == null ||
                        (filter.getOutOfStock() && p.getStock() <= 0))
                .collect(Collectors.toList());

        // Pagination
        int start = (int) PageRequest.of(filter.getPage(), filter.getSize()).getOffset();
        int end = Math.min((start + filter.getSize()), filteredProducts.size());

        List<ProductDTO> pageContent = filteredProducts.subList(start, end).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(pageContent, PageRequest.of(filter.getPage(), filter.getSize()),
                filteredProducts.size());
    }

    public ProductDTO getProductsById(String id) {
        Product product = productRepository.findById(new ObjectId(id))
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return convertToDTO(product);
    }

    public ProductDTO createProduct(ProductDTO productDTO) {
        Product product = convertToEntity(productDTO);
        product.setCreatedDate(LocalDate.now());
        product.setOrders(0);
        if (product.getStock() == 0)
            product.setStock(0);
        Product savedProduct = productRepository.save(product);
        return convertToDTO(savedProduct);
    }

    public ProductDTO updateProduct(String id, ProductDTO productDTO) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        updateEntityFromDTO(existingProduct, productDTO);
        Product updatedProduct = productRepository.save(existingProduct);
        return convertToDTO(updatedProduct);
    }

    public void deletesProduct(String id) {
        productRepository.deleteById(new ObjectId(id));
    }

    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId().toHexString());
        dto.setName(product.getName());
        dto.setBrand(product.getBrand());
        dto.setCategory(product.getCategory());
        dto.setScent(product.getScent());
        dto.setOccasion(product.getOccasion());
        dto.setGender(product.getGender());
        dto.setPrice(product.getPrice());
        dto.setDiscountedPrice(product.getDiscountedPrice());
        dto.setStock(product.getStock());
        dto.setOrders(product.getOrders());
        dto.setCreatedDate(product.getCreatedDate());
        dto.setIsFeatured(product.getIsFeatured());
        dto.setIsOnSale(product.getIsOnSale());
        dto.setImageUrl(product.getImageUrl());
        dto.setSummary(product.getSummary());
        dto.setTaxIncluded(product.getTaxIncluded());
        return dto;
    }

    private Product convertToEntity(ProductDTO dto) {
        Product product = new Product();
        updateEntityFromDTO(product, dto);
        return product;
    }

    private void updateEntityFromDTO(Product product, ProductDTO dto) {
        product.setName(dto.getName());
        product.setBrand(dto.getBrand());
        product.setCategory(dto.getCategory());
        product.setScent(dto.getScent());
        product.setOccasion(dto.getOccasion());
        product.setGender(dto.getGender());
        product.setPrice(dto.getPrice());
        product.setDiscountedPrice(dto.getDiscountedPrice());
        product.setStock(dto.getStock());
        product.setOrders(dto.getOrders());
        product.setIsFeatured(dto.getIsFeatured());
        product.setIsOnSale(dto.getIsOnSale());
        product.setImageUrl(dto.getImageUrl());
        product.setSummary(dto.getSummary());
        product.setTaxIncluded(dto.getTaxIncluded());
    }
}
