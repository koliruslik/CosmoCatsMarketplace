package com.team.cosmocats.dto;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String name,
        BigDecimal price,
        CategoryResponse category
) {
}
