package com.example.ecom.service;

import com.example.ecom.data.CartItem;
import com.example.ecom.data.Order;
import com.example.ecom.data.Product;
import com.example.ecom.data.User;
import com.example.ecom.dto.OrderResponse;
import com.example.ecom.repository.OrderRepository;
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
class OrderServiceTest {

    @Mock
    private CartService cartService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    private User user;
    private Product product;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);

        product = new Product();
        product.setId(10L);
        product.setPrice(BigDecimal.valueOf(50));
    }

    @Test
    void createOrder_returnsEmpty_whenCartIsEmpty() {
        when(cartService.getCart("1")).thenReturn(List.of());

        Optional<OrderResponse> result = orderService.createOrder("1");

        assertThat(result).isEmpty();
        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrder_returnsEmpty_whenUserNotFound() {
        CartItem item = new CartItem();
        item.setProduct(product);
        item.setQuantity(2);
        item.setPrice(BigDecimal.valueOf(100));

        when(cartService.getCart("1")).thenReturn(List.of(item));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<OrderResponse> result = orderService.createOrder("1");

        assertThat(result).isEmpty();
    }

    @Test
    void createOrder_calculatesTotalAndClearsCart_onSuccess() {
        CartItem item1 = new CartItem();
        item1.setProduct(product);
        item1.setQuantity(2);
        item1.setPrice(BigDecimal.valueOf(100));

        CartItem item2 = new CartItem();
        item2.setProduct(product);
        item2.setQuantity(1);
        item2.setPrice(BigDecimal.valueOf(50));

        when(cartService.getCart("1")).thenReturn(List.of(item1, item2));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(100L);
            return order;
        });

        Optional<OrderResponse> result = orderService.createOrder("1");

        assertThat(result).isPresent();
        assertThat(result.get().getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(150));
        assertThat(result.get().getItems()).hasSize(2);
        verify(cartService).clearCart("1");
    }
}
