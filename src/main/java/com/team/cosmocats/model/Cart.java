package com.team.cosmocats.model;

import lombok.*;

import java.math.BigDecimal;
import java.util.*;

@AllArgsConstructor
@RequiredArgsConstructor
@Getter
public class Cart {
    @NonNull
    private Long id;
    @NonNull
    private Map<Long, CartItem> items = new LinkedHashMap<>();
    
    public void addItem(Product product) {
        this.validateProduct(product);
               
        var productId = product.getId();
        var item = items.get(productId);
        if (item != null) {
            item.setQuantity(item.getQuantity() + 1);
            return;
        }
        
        items.put(productId, new CartItem(product, 1));
    }

    public void removeItem(Product product) {
        this.validateProduct(product);
        
        var productId = product.getId();
        items.remove(productId);
    }
    
    public void decreaseQuantity(Product product) {
        this.validateProduct(product);

        var item = items.get(product.getId());
        if (item == null) {
            return;
        }
        
        if (item.getQuantity() <= 1) {
            items.remove(product.getId());
            return;
        }
        
        item.setQuantity(item.getQuantity() - 1);
    }
    
    public BigDecimal calculateTotal() {
        return items.values()
                .stream()
                .map(item -> item.getProduct()
                        .getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    private void validateProduct(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        if (product.getId() == null) {
            throw new IllegalArgumentException("Product id cannot be null");
        }
    }
}
