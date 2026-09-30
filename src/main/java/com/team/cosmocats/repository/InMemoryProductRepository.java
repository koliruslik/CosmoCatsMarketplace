package com.team.cosmocats.repository;

import com.team.cosmocats.model.Product;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryProductRepository implements ProductRepository {
    private final Map<Long, Product> products = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    @Override
    public Optional<Product> findById(Long id) {
        return Optional.ofNullable(products.get(id));
    }
    
    @Override
    public List<Product> findAll(int page, int size) {
        long offset = (long) page * size;
        
        return products.values()
                .stream()
                .sorted(Comparator.comparing(Product::getId))
                .skip(offset)
                .limit(size)
                .toList();
    }
    
    @Override
    public long count() {
        return products.size();
    }
    
    @Override
    public Product save(Product product) {
        Objects.requireNonNull(product, "Product cannot be null");
        
        if (product.getId() != null) {
            products.put(product.getId(), product);
            return product;
        }
        
        long id = nextId.getAndIncrement();
        
        var savedProduct = new Product(
                id,
                product.getName(),
                product.getPrice(),
                product.getCategory()
        );
        
        products.put(id, savedProduct);
        return savedProduct;
    }
    
    @Override
    public void deleteById(Long id) {
        products.remove(id);
    }
}
