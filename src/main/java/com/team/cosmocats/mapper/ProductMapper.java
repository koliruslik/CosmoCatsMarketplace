package com.team.cosmocats.mapper;

import com.team.cosmocats.dto.CategoryResponse;
import com.team.cosmocats.dto.ProductResponse;
import com.team.cosmocats.model.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {
    public ProductResponse toResponse(final Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                new CategoryResponse(
                        product.getCategory().getId(),
                        product.getCategory().getName()
                )
        );
    }
}
