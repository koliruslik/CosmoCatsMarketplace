package com.team.cosmocats.repository;

import com.team.cosmocats.model.Category;

import java.util.Optional;

public interface CategoryRepository {
    Optional<Category> findById(Long id);
    
}
