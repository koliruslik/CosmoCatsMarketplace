package com.team.cosmocats.service;

import com.team.cosmocats.dto.CategoryResponse;
import com.team.cosmocats.dto.CreateProductRequest;
import com.team.cosmocats.dto.ProductResponse;
import com.team.cosmocats.dto.UpdateProductRequest;
import com.team.cosmocats.exception.CategoryNotFoundException;
import com.team.cosmocats.exception.ProductNotFoundException;
import com.team.cosmocats.mapper.ProductMapper;
import com.team.cosmocats.model.Category;
import com.team.cosmocats.model.Product;
import com.team.cosmocats.repository.CategoryRepository;
import com.team.cosmocats.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
    @Mock
    private ProductRepository productRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private ProductMapper productMapper;
    @InjectMocks
    private ProductService productService;

    private final Category category = new Category(1L, "Food");
    private final Product product = new Product(7L, "Cosmic tuna", new BigDecimal("12.50"), category);
    private final ProductResponse response = new ProductResponse(
            7L, "Cosmic tuna", new BigDecimal("12.50"), new CategoryResponse(1L, "Food"));

    @Test
    void returnsProductById() {
        when(productRepository.findById(7L)).thenReturn(Optional.of(product));
        when(productMapper.toResponse(product)).thenReturn(response);

        assertThat(productService.getProductById(7L)).isEqualTo(response);
    }

    @Test
    void rejectsMissingProduct() {
        when(productRepository.findById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(7L))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessage("Product with id 7 was not found");
        verifyNoInteractions(productMapper);
    }

    @ParameterizedTest
    @CsvSource({"0, 20, 0, 0", "0, 20, 20, 1", "1, 20, 21, 2", "3, 10, 31, 4"})
    void calculatesPaginationMetadata(int page, int size, long count, int totalPages) {
        when(productRepository.findAll(page, size)).thenReturn(List.of());
        when(productRepository.count()).thenReturn(count);

        var result = productService.getProducts(page, size);

        assertThat(result.content()).isEmpty();
        assertThat(result.page()).isEqualTo(page);
        assertThat(result.size()).isEqualTo(size);
        assertThat(result.totalElements()).isEqualTo(count);
        assertThat(result.totalPages()).isEqualTo(totalPages);
    }

    @Test
    void mapsProductsInPage() {
        when(productRepository.findAll(0, 20)).thenReturn(List.of(product));
        when(productRepository.count()).thenReturn(1L);
        when(productMapper.toResponse(product)).thenReturn(response);

        assertThat(productService.getProducts(0, 20).content()).containsExactly(response);
    }

    @Test
    void createsProductUsingExistingCategory() {
        var request = new CreateProductRequest("Cosmic tuna", new BigDecimal("12.50"), 1L);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(productMapper.toResponse(product)).thenReturn(response);

        assertThat(productService.createProduct(request)).isEqualTo(response);

        var captured = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captured.capture());
        assertThat(captured.getValue().getId()).isNull();
        assertThat(captured.getValue().getName()).isEqualTo(request.name());
        assertThat(captured.getValue().getPrice()).isEqualByComparingTo(request.price());
        assertThat(captured.getValue().getCategory()).isSameAs(category);
    }

    @Test
    void rejectsCreationWithUnknownCategory() {
        var request = new CreateProductRequest("Cosmic tuna", BigDecimal.ONE, 99L);
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.createProduct(request))
                .isInstanceOf(CategoryNotFoundException.class)
                .hasMessage("Category with id 99 was not found");
        verifyNoInteractions(productRepository, productMapper);
    }

    @Test
    void updatesProductAndCategory() {
        var newCategory = new Category(2L, "Toys");
        var request = new UpdateProductRequest("Moon ball", new BigDecimal("4.25"), 2L);
        var updatedResponse = new ProductResponse(7L, request.name(), request.price(),
                new CategoryResponse(2L, "Toys"));
        when(productRepository.findById(7L)).thenReturn(Optional.of(product));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(newCategory));
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toResponse(product)).thenReturn(updatedResponse);

        assertThat(productService.updateProduct(7L, request)).isEqualTo(updatedResponse);
        assertThat(product.getName()).isEqualTo(request.name());
        assertThat(product.getPrice()).isEqualByComparingTo(request.price());
        assertThat(product.getCategory()).isSameAs(newCategory);
        verify(productRepository).save(product);
    }

    @Test
    void rejectsUpdateOfMissingProduct() {
        when(productRepository.findById(7L)).thenReturn(Optional.empty());
        var request = new UpdateProductRequest("Moon ball", BigDecimal.ONE, 2L);

        assertThatThrownBy(() -> productService.updateProduct(7L, request))
                .isInstanceOf(ProductNotFoundException.class);
        verifyNoInteractions(categoryRepository, productMapper);
        verify(productRepository, never()).save(any());
    }

    @Test
    void leavesProductUnchangedWhenUpdateCategoryIsUnknown() {
        when(productRepository.findById(7L)).thenReturn(Optional.of(product));
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());
        var request = new UpdateProductRequest("Moon ball", BigDecimal.ONE, 99L);

        assertThatThrownBy(() -> productService.updateProduct(7L, request))
                .isInstanceOf(CategoryNotFoundException.class);
        assertThat(product.getName()).isEqualTo("Cosmic tuna");
        assertThat(product.getPrice()).isEqualByComparingTo("12.50");
        assertThat(product.getCategory()).isSameAs(category);
        verify(productRepository, never()).save(any());
        verifyNoInteractions(productMapper);
    }

    @Test
    void deletesProductById() {
        productService.deleteProduct(7L);

        verify(productRepository).deleteById(7L);
        verifyNoInteractions(categoryRepository, productMapper);
    }
}
