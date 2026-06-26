package com.example.ecom.controller;

import com.example.ecom.data.User;
import com.example.ecom.dto.AuthResponse;
import com.example.ecom.dto.LoginRequest;
import com.example.ecom.dto.RegisterRequest;
import com.example.ecom.security.JwtUtil;
import com.example.ecom.security.UserPrincipal;
import com.example.ecom.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        return userService.registerUser(request)
                .map(user -> {
                    UserPrincipal principal = new UserPrincipal(user);
                    String token = jwtUtil.generateToken(principal);
                    AuthResponse response = new AuthResponse(token, user.getId(), user.getEmail(), user.getRole());
                    return ResponseEntity.status(HttpStatus.CREATED).body(response);
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.CONFLICT).body(new AuthResponse(null, null, "Email already registered", null)));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
            User user = principal.getUser();
            String token = jwtUtil.generateToken(principal);

            return ResponseEntity.ok(new AuthResponse(token, user.getId(), user.getEmail(), user.getRole()));
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthResponse(null, null, "Invalid email or password", null));
        }
    }
}