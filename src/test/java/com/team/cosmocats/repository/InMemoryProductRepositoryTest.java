package com.team.cosmocats.repository;

import com.team.cosmocats.model.Category;
import com.team.cosmocats.model.Product;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class InMemoryProductRepositoryTest {
    private final InMemoryProductRepository repository = new InMemoryProductRepository();
    private final Category category = new Category(1L, "Food");

    @Test
    void assignsUniqueIdsAndFindsSavedProducts() {
        var first = repository.save(product("Tuna"));
        var second = repository.save(product("Salmon"));

        assertThat(first.getId()).isNotEqualTo(second.getId());
        assertThat(repository.findById(first.getId())).contains(first);
        assertThat(repository.findById(second.getId())).contains(second);
        assertThat(repository.count()).isEqualTo(2);
    }

    @Test
    void replacesExistingProductWithoutChangingCount() {
        var saved = repository.save(product("Tuna"));
        var replacement = new Product(saved.getId(), "Salmon", BigDecimal.TEN, category);

        assertThat(repository.save(replacement)).isSameAs(replacement);
        assertThat(repository.findById(saved.getId())).contains(replacement);
        assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    void returnsPagesOrderedById() {
        var first = repository.save(product("Tuna"));
        var second = repository.save(product("Salmon"));
        var third = repository.save(product("Cod"));

        assertThat(repository.findAll(0, 2)).containsExactly(first, second);
        assertThat(repository.findAll(1, 2)).containsExactly(third);
        assertThat(repository.findAll(2, 2)).isEmpty();
        assertThat(repository.findAll(Integer.MAX_VALUE, 100)).isEmpty();
    }

    @Test
    void deletesExistingProductAndAllowsRepeatedDeletion() {
        var saved = repository.save(product("Tuna"));

        repository.deleteById(saved.getId());
        repository.deleteById(saved.getId());

        assertThat(repository.findById(saved.getId())).isEmpty();
        assertThat(repository.count()).isZero();
    }

    @Test
    void returnsEmptyResultForUnknownId() {
        assertThat(repository.findById(99L)).isEmpty();
        assertThat(repository.findAll(0, 20)).isEmpty();
    }

    @Test
    void rejectsNullProduct() {
        assertThatNullPointerException().isThrownBy(() -> repository.save(null));
        assertThat(repository.count()).isZero();
    }

    private Product product(String name) {
        return new Product(name, BigDecimal.ONE, category);
    }
}
