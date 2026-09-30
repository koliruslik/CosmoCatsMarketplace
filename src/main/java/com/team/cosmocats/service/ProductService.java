package com.team.cosmocats.service;

import com.team.cosmocats.dto.*;
import com.team.cosmocats.exception.CategoryNotFoundException;
import com.team.cosmocats.exception.ProductNotFoundException;
import com.team.cosmocats.mapper.ProductMapper;
import com.team.cosmocats.model.Product;
import com.team.cosmocats.repository.CategoryRepository;
import com.team.cosmocats.repository.ProductRepository;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;
    
    public ProductResponse getProductById(Long id) {
        var product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        
        return productMapper.toResponse(product);
    }
    
    public ProductPageResponse getProducts(@Min(0) int page, @Min(1) @Max(100) int size) {
        var products = productRepository.findAll(page, size)
                .stream()
                .map(productMapper::toResponse)
                .toList();
        
        long totalElements = productRepository.count();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        
        return new ProductPageResponse(
                products,
                page,
                size,
                totalElements,
                totalPages
        );
    }
    
    public ProductResponse createProduct(CreateProductRequest request) {
        var category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new CategoryNotFoundException(request.categoryId()));
        
        var product = new Product(
                request.name(),
                request.price(),
                category);
        
        var savedProduct = productRepository.save(product);
        return productMapper.toResponse(savedProduct);
    }
    
    public ProductResponse updateProduct(Long id, UpdateProductRequest request) {
        var product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        
        var category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new CategoryNotFoundException(request.categoryId()));
        
        product.setName(request.name());
        product.setPrice(request.price());
        product.setCategory(category);
        
        var savedProduct = productRepository.save(product);
        return productMapper.toResponse(savedProduct);
    }
    
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}
