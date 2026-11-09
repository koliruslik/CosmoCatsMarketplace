package com.team.cosmocats.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class CartTest {
    private final Cart cart = new Cart(1L);
    private final Product tuna = new Product(1L, "Tuna", new BigDecimal("12.50"), new Category(1L, "Food"));

    @Test
    void combinesRepeatedProductsAndCalculatesTotal() {
        var toy = new Product(2L, "Moon ball", new BigDecimal("3.25"), new Category(2L, "Toys"));

        cart.addItem(tuna);
        cart.addItem(tuna);
        cart.addItem(toy);

        assertThat(cart.getItems()).hasSize(2);
        assertThat(cart.getItems().get(1L).getQuantity()).isEqualTo(2);
        assertThat(cart.calculateTotal()).isEqualByComparingTo("28.25");
    }

    @Test
    void decreasesQuantityAndRemovesLastUnit() {
        cart.addItem(tuna);
        cart.addItem(tuna);

        cart.decreaseQuantity(tuna);
        assertThat(cart.getItems().get(1L).getQuantity()).isEqualTo(1);

        cart.decreaseQuantity(tuna);
        cart.decreaseQuantity(tuna);
        assertThat(cart.getItems()).isEmpty();
        assertThat(cart.calculateTotal()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void removesAllUnitsOfProduct() {
        cart.addItem(tuna);
        cart.addItem(tuna);

        cart.removeItem(tuna);
        cart.removeItem(tuna);

        assertThat(cart.getItems()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("invalidProducts")
    void rejectsNullOrUnsavedProducts(Product product) {
        assertThatIllegalArgumentException().isThrownBy(() -> cart.addItem(product));
        assertThatIllegalArgumentException().isThrownBy(() -> cart.removeItem(product));
        assertThatIllegalArgumentException().isThrownBy(() -> cart.decreaseQuantity(product));
        assertThat(cart.getItems()).isEmpty();
    }

    static Stream<Product> invalidProducts() {
        return Stream.of(null, new Product("Tuna", BigDecimal.ONE, new Category(1L, "Food")));
    }
}
