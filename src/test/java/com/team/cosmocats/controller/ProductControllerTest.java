package com.team.cosmocats.controller;

import com.team.cosmocats.dto.CategoryResponse;
import com.team.cosmocats.dto.CreateProductRequest;
import com.team.cosmocats.dto.ProductPageResponse;
import com.team.cosmocats.dto.ProductResponse;
import com.team.cosmocats.dto.UpdateProductRequest;
import com.team.cosmocats.exception.CategoryNotFoundException;
import com.team.cosmocats.exception.ProductNotFoundException;
import com.team.cosmocats.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
class ProductControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private ProductService productService;

    private static final String VALID_BODY = """
            {"name":"Cosmic tuna","price":12.50,"categoryId":1}
            """;
    private final ProductResponse product = new ProductResponse(
            7L, "Cosmic tuna", new BigDecimal("12.50"), new CategoryResponse(1L, "Food"));

    @Test
    void listsProductsUsingDefaultPagination() throws Exception {
        when(productService.getProducts(0, 20))
                .thenReturn(new ProductPageResponse(List.of(product), 0, 20, 1, 1));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(7))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @ParameterizedTest
    @CsvSource({"0,1", "1,100", "2147483647,100"})
    void acceptsValidPagination(int page, int size) throws Exception {
        when(productService.getProducts(page, size))
                .thenReturn(new ProductPageResponse(List.of(), page, size, 0, 0));

        mockMvc.perform(get("/api/products").param("page", String.valueOf(page))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
        verify(productService).getProducts(page, size);
    }

    @ParameterizedTest
    @CsvSource({"page,-1", "size,0", "size,-1", "size,101"})
    void rejectsInvalidPagination(String parameter, String value) throws Exception {
        mockMvc.perform(get("/api/products").param(parameter, value))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400));
        verifyNoInteractions(productService);
    }

    @ParameterizedTest
    @CsvSource({"page,text", "size,1.5", "page,2147483648"})
    void rejectsNonIntegerPagination(String parameter, String value) throws Exception {
        mockMvc.perform(get("/api/products").param(parameter, value))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid parameter"))
                .andExpect(jsonPath("$.detail").value("Parameter '%s' has an invalid value".formatted(parameter)));
        verifyNoInteractions(productService);
    }

    @Test
    void returnsProduct() throws Exception {
        when(productService.getProductById(7L)).thenReturn(product);

        mockMvc.perform(get("/api/products/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Cosmic tuna"))
                .andExpect(jsonPath("$.price").value(12.50))
                .andExpect(jsonPath("$.category.id").value(1));
    }

    @Test
    void returnsProblemForMissingProduct() throws Exception {
        when(productService.getProductById(7L)).thenThrow(new ProductNotFoundException(7L));

        mockMvc.perform(get("/api/products/7"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Product Not Found"))
                .andExpect(jsonPath("$.detail").value("Product with id 7 was not found"))
                .andExpect(jsonPath("$.instance").value("/api/products/7"));
    }

    @Test
    void createsProductWithLocationHeader() throws Exception {
        when(productService.createProduct(any())).thenReturn(product);

        mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/products/7"))
                .andExpect(jsonPath("$.id").value(7));
        verify(productService).createProduct(new CreateProductRequest("Cosmic tuna", new BigDecimal("12.50"), 1L));
    }

    @Test
    void updatesProduct() throws Exception {
        when(productService.updateProduct(eq(7L), any())).thenReturn(product);

        mockMvc.perform(put("/api/products/7").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7));
        verify(productService).updateProduct(7L,
                new UpdateProductRequest("Cosmic tuna", new BigDecimal("12.50"), 1L));
    }

    @Test
    void returnsNotFoundWhenUpdatingMissingProduct() throws Exception {
        when(productService.updateProduct(eq(7L), any())).thenThrow(new ProductNotFoundException(7L));

        mockMvc.perform(put("/api/products/7").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Product with id 7 was not found"));
    }

    @Test
    void rejectsUpdateWithUnknownCategory() throws Exception {
        when(productService.updateProduct(eq(7L), any())).thenThrow(new CategoryNotFoundException(1L));

        mockMvc.perform(put("/api/products/7").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid category"));
    }

    @ParameterizedTest
    @CsvSource({"POST,/api/products", "PUT,/api/products/7"})
    void acceptsMaximumNameLengthAndMinimumPrice(HttpMethod method, String path) throws Exception {
        var name = "x".repeat(100);
        var body = "{\"name\":\"" + name + "\",\"price\":0.01,\"categoryId\":1}";
        if (method == HttpMethod.POST) {
            when(productService.createProduct(any())).thenReturn(product);
        } else {
            when(productService.updateProduct(eq(7L), any())).thenReturn(product);
        }

        mockMvc.perform(request(method, path).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().is(method == HttpMethod.POST ? 201 : 200));
        if (method == HttpMethod.POST) {
            verify(productService).createProduct(new CreateProductRequest(name, new BigDecimal("0.01"), 1L));
        } else {
            verify(productService).updateProduct(7L, new UpdateProductRequest(name, new BigDecimal("0.01"), 1L));
        }
    }

    @Test
    void rejectsUnknownCategory() throws Exception {
        when(productService.createProduct(any())).thenThrow(new CategoryNotFoundException(1L));

        mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid category"))
                .andExpect(jsonPath("$.detail").value("Category with id 1 was not found"));
    }

    @ParameterizedTest
    @MethodSource("invalidBodies")
    void rejectsInvalidCreateAndUpdateBodies(HttpMethod method, String path, String body, String field) throws Exception {
        mockMvc.perform(request(method, path).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.detail").value(containsString(field + ":")));
        verifyNoInteractions(productService);
    }

    static Stream<Arguments> invalidBodies() {
        var bodies = List.of(
                new InvalidBody("{\"price\":1,\"categoryId\":1}", "name"),
                new InvalidBody("{\"name\":\" \",\"price\":1,\"categoryId\":1}", "name"),
                new InvalidBody("{\"name\":\"" + "x".repeat(101) + "\",\"price\":1,\"categoryId\":1}", "name"),
                new InvalidBody("{\"name\":\"Tuna\",\"categoryId\":1}", "price"),
                new InvalidBody("{\"name\":\"Tuna\",\"price\":0,\"categoryId\":1}", "price"),
                new InvalidBody("{\"name\":\"Tuna\",\"price\":-1,\"categoryId\":1}", "price"),
                new InvalidBody("{\"name\":\"Tuna\",\"price\":0.001,\"categoryId\":1}", "price"),
                new InvalidBody("{\"name\":\"Tuna\",\"price\":1}", "categoryId"),
                new InvalidBody("{\"name\":\"Tuna\",\"price\":1,\"categoryId\":0}", "categoryId"),
                new InvalidBody("{\"name\":\"Tuna\",\"price\":1,\"categoryId\":-1}", "categoryId")
        );
        return Stream.of(HttpMethod.POST, HttpMethod.PUT).flatMap(method -> bodies.stream()
                .map(body -> Arguments.of(method, method == HttpMethod.POST ? "/api/products" : "/api/products/7",
                        body.json(), body.field())));
    }

    private record InvalidBody(String json, String field) {
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "{", "null", "{\"price\":\"invalid\"}"})
    void rejectsUnreadableBodies(String body) throws Exception {
        mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid request body"));
        verifyNoInteractions(productService);
    }

    @ParameterizedTest
    @CsvSource({"GET,0", "GET,-1", "PUT,0", "PUT,-1", "DELETE,0", "DELETE,-1"})
    void rejectsNonPositiveProductIds(String method, String id) throws Exception {
        mockMvc.perform(request(HttpMethod.valueOf(method), "/api/products/" + id)
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"));
        verifyNoInteractions(productService);
    }

    @Test
    void rejectsNonNumericProductId() throws Exception {
        mockMvc.perform(get("/api/products/not-a-number"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid parameter"));
        verifyNoInteractions(productService);
    }

    @Test
    void deletesProductWithoutResponseBody() throws Exception {
        mockMvc.perform(delete("/api/products/7"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        verify(productService).deleteProduct(7L);
    }
}
