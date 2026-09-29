package com.team.cosmocats.model;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.Objects;

@Getter
public final class OrderItem {
    private final Long productId;
    private final String productName;
    private final BigDecimal unitPrice;
    private final int quantity;

    public OrderItem(Long productId, String productName, BigDecimal unitPrice, int quantity) {
        this.productId = Objects.requireNonNull(productId, "Product id cannot be null");
        this.productName = requireName(productName);
        this.unitPrice = requirePositivePrice(unitPrice);
        this.quantity = requirePositiveQuantity(quantity);
    }

    public static OrderItem from(CartItem cartItem) {
        Objects.requireNonNull(cartItem, "Cart item cannot be null");

        var product = cartItem.getProduct();
        return new OrderItem(
                product.getId(),
                product.getName(),
                product.getPrice(),
                cartItem.getQuantity()
        );
    }

    public BigDecimal calculateSubtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    private static String requireName(String productName) {
        Objects.requireNonNull(productName, "Product name cannot be null");
        if (productName.isBlank()) {
            throw new IllegalArgumentException("Product name cannot be blank");
        }
        return productName;
    }

    private static BigDecimal requirePositivePrice(BigDecimal unitPrice) {
        Objects.requireNonNull(unitPrice, "Unit price cannot be null");
        if (unitPrice.signum() <= 0) {
            throw new IllegalArgumentException("Unit price must be greater than zero");
        }
        return unitPrice;
    }

    private static int requirePositiveQuantity(int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }
        return quantity;
    }
}
