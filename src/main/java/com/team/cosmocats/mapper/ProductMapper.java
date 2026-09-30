package com.team.cosmocats.mapper;

import com.team.cosmocats.dto.CategoryResponse;
import com.team.cosmocats.dto.ProductResponse;
import com.team.cosmocats.model.Category;
import com.team.cosmocats.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface ProductMapper {
    ProductResponse toResponse(Product product);

    CategoryResponse toResponse(Category category);
}
