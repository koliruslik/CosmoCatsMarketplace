package com.team.cosmocats.repository;

import com.team.cosmocats.model.Category;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;

@Repository
public class InMemoryCategoryRepository implements CategoryRepository {
    private final Map<Long, Category> categories = Map.of(
            1L, new Category(1L, "Food"),
            2L, new Category(2L, "Toys"),
            3L, new Category(3L, "Spaceships")
    );
    
    @Override
    public Optional<Category> findById(Long id) {
        return Optional.ofNullable(categories.get(id));
    }
}
