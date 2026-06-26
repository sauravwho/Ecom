package com.example.ecom.service;

import com.example.ecom.data.Product;
import com.example.ecom.dto.ProductRequest;
import com.example.ecom.dto.ProductResponse;
import com.example.ecom.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void createProduct_savesAndReturnsMappedResponse() {
        ProductRequest request = new ProductRequest();
        request.setName("Mechanical Keyboard");
        request.setPrice(BigDecimal.valueOf(2500));
        request.setStockQuantity(10);

        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            p.setId(1L);
            return p;
        });

        ProductResponse response = productService.createProduct(request);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Mechanical Keyboard");
        assertThat(response.getActive()).isTrue();
    }

    @Test
    void updateProduct_returnsEmpty_whenProductNotFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<ProductResponse> result = productService.updateProduct(99L, new ProductRequest());

        assertThat(result).isEmpty();
    }

    @Test
    void updateProduct_updatesFields_whenProductExists() {
        Product existing = new Product();
        existing.setId(1L);
        existing.setName("Old Name");
        existing.setPrice(BigDecimal.valueOf(100));
        existing.setStockQuantity(5);

        ProductRequest request = new ProductRequest();
        request.setName("New Name");
        request.setPrice(BigDecimal.valueOf(150));
        request.setStockQuantity(3);

        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepository.save(any(Product.class))).thenReturn(existing);

        Optional<ProductResponse> result = productService.updateProduct(1L, request);

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("New Name");
        assertThat(result.get().getPrice()).isEqualByComparingTo(BigDecimal.valueOf(150));
    }

    @Test
    void deleteProduct_softDeletes_setsActiveFalse() {
        Product existing = new Product();
        existing.setId(1L);
        existing.setActive(true);

        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepository.save(any(Product.class))).thenReturn(existing);

        boolean result = productService.deleteProduct(1L);

        assertThat(result).isTrue();
        assertThat(existing.getActive()).isFalse();
        verify(productRepository).save(existing);
    }

    @Test
    void deleteProduct_returnsFalse_whenNotFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        boolean result = productService.deleteProduct(99L);

        assertThat(result).isFalse();
    }
}
