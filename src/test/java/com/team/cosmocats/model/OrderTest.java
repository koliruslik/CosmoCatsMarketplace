package com.team.cosmocats.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {
    private final OrderItem item = new OrderItem(1L, "Tuna", new BigDecimal("12.50"), 2);

    @Test
    void createsOrderWithCalculatedTotalAndDefensiveCopy() {
        var items = new ArrayList<>(List.of(item));
        var createdAt = Instant.parse("2026-10-01T10:00:00Z");

        var order = new Order(7L, items, OrderStatus.CREATED, createdAt);
        items.clear();

        assertThat(order.getId()).isEqualTo(7L);
        assertThat(order.getItems()).containsExactly(item);
        assertThat(order.getCreatedAt()).isEqualTo(createdAt);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(order.getTotalPrice()).isEqualByComparingTo("25.00");
        assertThatThrownBy(() -> order.getItems().clear()).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void snapshotsCartProductDetailsAndQuantity() {
        var product = new Product(1L, "Tuna", new BigDecimal("12.50"), new Category(1L, "Food"));
        var cart = new Cart(1L);
        cart.addItem(product);
        cart.addItem(product);

        var order = Order.fromCart(cart);
        product.setName("Salmon");
        product.setPrice(BigDecimal.ONE);
        cart.removeItem(product);

        assertThat(order.getId()).isNull();
        assertThat(order.getCreatedAt()).isNotNull();
        assertThat(order.getItems()).hasSize(1);
        assertThat(order.getItems().get(0).getProductName()).isEqualTo("Tuna");
        assertThat(order.getItems().get(0).getQuantity()).isEqualTo(2);
        assertThat(order.getItems().get(0).getUnitPrice()).isEqualByComparingTo("12.50");
        assertThat(order.getTotalPrice()).isEqualByComparingTo("25.00");
    }

    @Test
    void allowsPaymentOfCreatedOrder() {
        var order = new Order(List.of(item));

        order.markAsPaid();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
    }

    @Test
    void allowsCancellationOfCreatedOrder() {
        var order = new Order(List.of(item));

        order.cancel();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @ParameterizedTest
    @EnumSource(value = OrderStatus.class, names = {"PAID", "CANCELLED"})
    void rejectsChangesToFinalStatus(OrderStatus status) {
        var order = new Order(7L, List.of(item), status, Instant.now());

        assertThatIllegalStateException().isThrownBy(order::markAsPaid);
        assertThatIllegalStateException().isThrownBy(order::cancel);
        assertThat(order.getStatus()).isEqualTo(status);
    }

    @Test
    void rejectsEmptyOrNullOrderItems() {
        assertThatNullPointerException().isThrownBy(() -> new Order(null));
        assertThatIllegalArgumentException().isThrownBy(() -> new Order(List.of()));
        assertThatIllegalArgumentException().isThrownBy(() -> new Order(Arrays.asList(item, null)));
        assertThatNullPointerException().isThrownBy(() -> Order.fromCart(null));
        assertThatIllegalArgumentException().isThrownBy(() -> Order.fromCart(new Cart(1L)));
    }

    @Test
    void rejectsMissingStatusOrCreationTime() {
        assertThatNullPointerException().isThrownBy(() -> new Order(1L, List.of(item), null, Instant.now()));
        assertThatNullPointerException().isThrownBy(() -> new Order(1L, List.of(item), OrderStatus.CREATED, null));
    }
}
