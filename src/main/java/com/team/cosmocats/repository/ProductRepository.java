package com.team.cosmocats.repository;

import com.team.cosmocats.model.Product;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    Optional<Product> findById(Long id);
    List<Product> findAll(int page, int size);
    long count();
    Product save(Product product);
    void deleteById(Long id);
}
