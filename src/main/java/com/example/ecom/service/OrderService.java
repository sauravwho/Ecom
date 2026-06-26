package com.example.ecom.service;

import com.example.ecom.data.*;
import com.example.ecom.dto.OrderItemDTO;
import com.example.ecom.dto.OrderResponse;
import com.example.ecom.repository.OrderRepository;
import com.example.ecom.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final CartService cartService;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    public Optional<OrderResponse> createOrder(String userId) {

        //validate for cart items
        List<CartItem> cartItem = cartService.getCart(userId);
        if(cartItem.isEmpty()){
            return Optional.empty();
        }

        //validate for user
        Optional<User> userOptional = userRepository.findById(Long.valueOf(userId));
        if(userOptional.isEmpty()){
            return Optional.empty();
        }
        User user = userOptional.get();

        //Calculate total price
        BigDecimal totalPrice = cartItem.stream()
                .map(CartItem::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        //Create order
        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.CONFIRMED);
        order.setTotalAmount(totalPrice);
        List<OrderItem> orderItems = cartItem.stream()
                .map(item -> new OrderItem(
                        null,
                        item.getProduct(),
                        item.getQuantity(),
                        item.getPrice(),
                        order
                ))
                .toList();
        order.setItems(orderItems);

        Order savedOrder = orderRepository.save(order);
        cartService.clearCart(userId);

        return Optional.of(mapOrderToResponse(savedOrder));
    }

    private OrderResponse mapOrderToResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getItems().stream()
                        .map(orderItem -> new OrderItemDTO(
                                orderItem.getId(),
                                orderItem.getProduct().getId(),
                                orderItem.getQuantity(),
                                orderItem.getPrice(),
                                orderItem.getPrice().multiply(new BigDecimal(orderItem.getQuantity()))
                        ))
                        .toList(),
                order.getCreatedAt()
        );
    }
}
