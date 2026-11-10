package com.team.cosmocats.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryCategoryRepositoryTest {
    private final InMemoryCategoryRepository repository = new InMemoryCategoryRepository();

    @ParameterizedTest
    @CsvSource({"1,Food", "2,Toys", "3,Spaceships"})
    void findsSeededCategories(long id, String name) {
        var category = repository.findById(id).orElseThrow();

        assertThat(category.getId()).isEqualTo(id);
        assertThat(category.getName()).isEqualTo(name);
    }

    @Test
    void returnsEmptyForUnknownCategory() {
        assertThat(repository.findById(99L)).isEmpty();
    }
}
