package com.example.ecom.dto;

import com.example.ecom.data.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String tokenType = "Bearer";
    private Long userId;
    private String email;
    private UserRole role;

    public AuthResponse(String token, Long userId, String email, UserRole role) {
        this.token = token;
        this.tokenType = "Bearer";
        this.userId = userId;
        this.email = email;
        this.role = role;
    }
}
