package com.example.ecom.dto;

import lombok.Data;

@Data
public class cartItemRequest {
    private Long productId;
    private Integer quantity;
}
