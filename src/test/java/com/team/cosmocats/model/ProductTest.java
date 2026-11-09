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

class ProductTest {
    private final Category category = new Category(1L, "Food");

    @Test
    void updatesProductDetails() {
        var product = new Product(7L, "Tuna", BigDecimal.ONE, category);
        var toys = new Category(2L, "Toys");

        product.setName("Moon ball");
        product.setPrice(BigDecimal.TEN);
        product.setCategory(toys);

        assertThat(product.getId()).isEqualTo(7L);
        assertThat(product.getName()).isEqualTo("Moon ball");
        assertThat(product.getPrice()).isEqualByComparingTo(BigDecimal.TEN);
        assertThat(product.getCategory()).isSameAs(toys);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t"})
    void rejectsInvalidNamesOnCreationAndUpdate(String name) {
        var product = new Product("Tuna", BigDecimal.ONE, category);
        var expected = name == null ? NullPointerException.class : IllegalArgumentException.class;

        assertThatThrownBy(() -> new Product(name, BigDecimal.ONE, category)).isInstanceOf(expected);
        assertThatThrownBy(() -> product.setName(name)).isInstanceOf(expected);
        assertThat(product.getName()).isEqualTo("Tuna");
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-0.01"})
    void rejectsNonPositivePrices(String price) {
        var product = new Product("Tuna", BigDecimal.ONE, category);

        assertThatIllegalArgumentException().isThrownBy(() -> new Product("Tuna", new BigDecimal(price), category));
        assertThatIllegalArgumentException().isThrownBy(() -> product.setPrice(new BigDecimal(price)));
        assertThat(product.getPrice()).isEqualByComparingTo(BigDecimal.ONE);
    }

    @Test
    void rejectsNullPriceAndCategory() {
        var product = new Product("Tuna", BigDecimal.ONE, category);

        assertThatNullPointerException().isThrownBy(() -> new Product("Tuna", null, category));
        assertThatNullPointerException().isThrownBy(() -> new Product("Tuna", BigDecimal.ONE, null));
        assertThatNullPointerException().isThrownBy(() -> product.setPrice(null));
        assertThatNullPointerException().isThrownBy(() -> product.setCategory(null));
        assertThat(product.getPrice()).isEqualByComparingTo(BigDecimal.ONE);
        assertThat(product.getCategory()).isSameAs(category);
    }
}
