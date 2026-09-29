package com.team.cosmocats.model;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.Objects;

@Getter
public class Product {
    private final Long id;
    private String name;
    private BigDecimal price;
    private Category category;

    public Product(String name, BigDecimal price, Category category) {
        this(null, name, price, category);
    }

    public Product(Long id, String name, BigDecimal price, Category category) {
        this.id = id;
        this.name = requireName(name);
        this.price = requirePositivePrice(price);
        this.category = requireCategory(category);
    }

    public void setName(String name) {
        this.name = requireName(name);
    }

    public void setPrice(BigDecimal price) {
        this.price = requirePositivePrice(price);
    }

    public void setCategory(Category category) {
        this.category = requireCategory(category);
    }

    private static String requireName(String name) {
        Objects.requireNonNull(name, "Product name cannot be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("Product name cannot be blank");
        }
        return name;
    }

    private static BigDecimal requirePositivePrice(BigDecimal price) {
        Objects.requireNonNull(price, "Product price cannot be null");
        if (price.signum() <= 0) {
            throw new IllegalArgumentException("Product price must be greater than zero");
        }
        return price;
    }

    private static Category requireCategory(Category category) {
        return Objects.requireNonNull(category, "Product category cannot be null");
    }
}
