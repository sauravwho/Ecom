package com.example.ecom.service;

import com.example.ecom.data.CartItem;
import com.example.ecom.data.Product;
import com.example.ecom.data.User;
import com.example.ecom.dto.cartItemRequest;
import com.example.ecom.repository.CartItemRepository;
import com.example.ecom.repository.ProductRepository;
import com.example.ecom.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CartService cartService;

    private User user;
    private Product product;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);

        product = new Product();
        product.setId(10L);
        product.setPrice(BigDecimal.valueOf(100));
        product.setStockQuantity(5);
    }

    @Test
    void addToCart_returnsFalse_whenProductDoesNotExist() {
        cartItemRequest request = new cartItemRequest();
        request.setProductId(99L);
        request.setQuantity(1);

        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        boolean result = cartService.addToCart("1", request);

        assertThat(result).isFalse();
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void addToCart_returnsFalse_whenRequestedQuantityExceedsStock() {
        cartItemRequest request = new cartItemRequest();
        request.setProductId(10L);
        request.setQuantity(10); // stock is only 5

        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        boolean result = cartService.addToCart("1", request);

        assertThat(result).isFalse();
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void addToCart_returnsFalse_whenUserDoesNotExist() {
        cartItemRequest request = new cartItemRequest();
        request.setProductId(10L);
        request.setQuantity(2);

        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        boolean result = cartService.addToCart("1", request);

        assertThat(result).isFalse();
    }

    @Test
    void addToCart_createsNewCartItem_whenNoneExists() {
        cartItemRequest request = new cartItemRequest();
        request.setProductId(10L);
        request.setQuantity(2);

        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartItemRepository.findByUserAndProduct(user, product)).thenReturn(null);

        boolean result = cartService.addToCart("1", request);

        assertThat(result).isTrue();
        verify(cartItemRepository).save(argThat(item ->
                item.getQuantity().equals(2) &&
                item.getPrice().compareTo(BigDecimal.valueOf(200)) == 0
        ));
    }

    @Test
    void addToCart_mergesQuantity_whenItemAlreadyInCart() {
        cartItemRequest request = new cartItemRequest();
        request.setProductId(10L);
        request.setQuantity(2);

        CartItem existing = new CartItem();
        existing.setQuantity(1);
        existing.setPrice(BigDecimal.valueOf(100));

        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartItemRepository.findByUserAndProduct(user, product)).thenReturn(existing);

        boolean result = cartService.addToCart("1", request);

        assertThat(result).isTrue();
        // 1 (existing) + 2 (new) = 3 total units, at price 100 each = 300
        verify(cartItemRepository).save(argThat(item ->
                item.getQuantity().equals(3) &&
                item.getPrice().compareTo(BigDecimal.valueOf(300)) == 0
        ));
    }

    @Test
    void getCart_returnsEmptyList_whenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        List<CartItem> cart = cartService.getCart("1");

        assertThat(cart).isEmpty();
    }

    @Test
    void getCart_returnsItems_whenUserExists() {
        CartItem item = new CartItem();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartItemRepository.findByUser(user)).thenReturn(List.of(item));

        List<CartItem> cart = cartService.getCart("1");

        assertThat(cart).hasSize(1);
    }

    @Test
    void deleteItemFromCart_returnsFalse_whenProductMissing() {
        when(productRepository.findById(10L)).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        boolean result = cartService.deleteItemFromCart("1", 10L);

        assertThat(result).isFalse();
        verify(cartItemRepository, never()).deleteByUserAndProduct(any(), any());
    }

    @Test
    void deleteItemFromCart_deletesAndReturnsTrue_whenBothExist() {
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        boolean result = cartService.deleteItemFromCart("1", 10L);

        assertThat(result).isTrue();
        verify(cartItemRepository).deleteByUserAndProduct(user, product);
    }
}
