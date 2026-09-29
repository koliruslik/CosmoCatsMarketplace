package com.team.cosmocats.model;

import lombok.Getter;
import lombok.NonNull;

import java.util.Objects;

@Getter
public class CartItem {
    @NonNull
    private final Product product;
    private int quantity;
    
    public CartItem(Product product, int quantity) {
        this.product = Objects.requireNonNull(product, "product cannot be null");
        this.setQuantity(quantity);
    }
    
    public void setQuantity(int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity cannot be less than 1");
        }
        this.quantity = quantity;
    }
}
