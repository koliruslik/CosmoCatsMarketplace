package com.team.cosmocats.mapper;

import com.team.cosmocats.dto.CategoryResponse;
import com.team.cosmocats.dto.ProductResponse;
import com.team.cosmocats.model.Category;
import com.team.cosmocats.model.Product;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ProductMapperTest {
    private final ProductMapper mapper = Mappers.getMapper(ProductMapper.class);

    @Test
    void mapsProductWithNestedCategory() {
        var category = new Category(1L, "Food");
        var product = new Product(7L, "Tuna", new BigDecimal("12.50"), category);

        assertThat(mapper.toResponse(product)).isEqualTo(new ProductResponse(
                7L, "Tuna", new BigDecimal("12.50"), new CategoryResponse(1L, "Food")));
    }

    @Test
    void mapsCategory() {
        assertThat(mapper.toResponse(new Category(2L, "Toys")))
                .isEqualTo(new CategoryResponse(2L, "Toys"));
    }

    @Test
    void preservesNullInputs() {
        assertThat(mapper.toResponse((Product) null)).isNull();
        assertThat(mapper.toResponse((Category) null)).isNull();
    }
}
