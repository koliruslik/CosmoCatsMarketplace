package com.team.cosmocats.controller;

import com.team.cosmocats.dto.CreateProductRequest;
import com.team.cosmocats.dto.ProductPageResponse;
import com.team.cosmocats.dto.ProductResponse;
import com.team.cosmocats.dto.UpdateProductRequest;
import com.team.cosmocats.service.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;
    
    @GetMapping
    public ProductPageResponse getProducts(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return productService.getProducts(page, size);
    }
    
    @GetMapping("/{productId}")
    public ProductResponse getProduct(
            @PathVariable @Positive Long productId) {
        return productService.getProductById(productId);
    }
    
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody CreateProductRequest request) {
        var product = productService.createProduct(request);
        
        var location = URI.create("/api/products/" + product.id());
        
        return ResponseEntity
                .created(location)
                .body(product);
    }
    
    @PutMapping("/{productId}")
    public ProductResponse updateProduct(
            @PathVariable @Positive Long productId,
            @Valid @RequestBody UpdateProductRequest request) {
        return productService.updateProduct(productId, request);
    }
    
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable @Positive Long productId) {
        productService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }
}
