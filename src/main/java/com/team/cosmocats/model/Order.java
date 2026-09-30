package com.team.cosmocats.model;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Getter
public final class Order {
    private final Long id;
    private final List<OrderItem> items;
    private final Instant createdAt;
    private final BigDecimal totalPrice;
    private OrderStatus status;

    public Order(List<OrderItem> items) {
        this(null, items, OrderStatus.CREATED, Instant.now());
    }

    public Order(Long id, List<OrderItem> items, OrderStatus status, Instant createdAt) {
        this.id = id;
        this.items = requireItems(items);
        this.status = requireStatus(status);
        this.createdAt = requireCreatedAt(createdAt);
        this.totalPrice = calculateTotal(this.items);
    }

    public static Order fromCart(Cart cart) {
        Objects.requireNonNull(cart, "Cart cannot be null");

        var orderItems = cart.getItems()
                .values()
                .stream()
                .map(OrderItem::from)
                .toList();

        return new Order(orderItems);
    }

    public void markAsPaid() {
        requireCreatedStatus("Only a created order can be paid");
        status = OrderStatus.PAID;
    }

    public void cancel() {
        requireCreatedStatus("Only a created order can be cancelled");
        status = OrderStatus.CANCELLED;
    }

    private void requireCreatedStatus(String message) {
        if (status != OrderStatus.CREATED) {
            throw new IllegalStateException(message);
        }
    }

    private static List<OrderItem> requireItems(List<OrderItem> items) {
        Objects.requireNonNull(items, "Order items cannot be null");
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Order cannot be empty");
        }
        if (items.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Order items cannot contain null values");
        }
        return List.copyOf(items);
    }

    private static OrderStatus requireStatus(OrderStatus status) {
        return Objects.requireNonNull(status, "Order status cannot be null");
    }

    private static Instant requireCreatedAt(Instant createdAt) {
        return Objects.requireNonNull(createdAt, "Creation time cannot be null");
    }

    private static BigDecimal calculateTotal(List<OrderItem> items) {
        return items.stream()
                .map(OrderItem::calculateSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
