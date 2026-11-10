package com.team.cosmocats.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderItemTest {
    @Test
    void calculatesSubtotal() {
        var item = new OrderItem(7L, "Tuna", new BigDecimal("12.50"), 3);

        assertThat(item.getProductId()).isEqualTo(7L);
        assertThat(item.calculateSubtotal()).isEqualByComparingTo("37.50");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t"})
    void rejectsInvalidProductNames(String name) {
        assertThatThrownBy(() -> new OrderItem(7L, name, BigDecimal.ONE, 1))
                .isInstanceOf(name == null ? NullPointerException.class : IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1"})
    void rejectsNonPositivePrice(String price) {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new OrderItem(7L, "Tuna", new BigDecimal(price), 1));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void rejectsNonPositiveQuantity(int quantity) {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new OrderItem(7L, "Tuna", BigDecimal.ONE, quantity));
    }

    @Test
    void rejectsMissingProductIdPriceOrCartItem() {
        assertThatNullPointerException().isThrownBy(() -> new OrderItem(null, "Tuna", BigDecimal.ONE, 1));
        assertThatNullPointerException().isThrownBy(() -> new OrderItem(7L, "Tuna", null, 1));
        assertThatNullPointerException().isThrownBy(() -> OrderItem.from(null));
    }

    @Test
    void rejectsInvalidCartItemQuantityWithoutChangingIt() {
        var product = new Product(7L, "Tuna", BigDecimal.ONE, new Category(1L, "Food"));
        var item = new CartItem(product, 2);

        assertThatIllegalArgumentException().isThrownBy(() -> item.setQuantity(0));
        assertThatIllegalArgumentException().isThrownBy(() -> new CartItem(product, -1));
        assertThatNullPointerException().isThrownBy(() -> new CartItem(null, 1));
        assertThat(item.getQuantity()).isEqualTo(2);
    }
}
