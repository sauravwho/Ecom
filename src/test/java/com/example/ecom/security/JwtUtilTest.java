package com.example.ecom.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private UserDetails userDetails;

    private static final String SECRET = "unit-test-secret-key-must-be-at-least-32-bytes-long";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(SECRET, 1000 * 60 * 60); // 1 hour
        userDetails = new User("saurav@example.com", "irrelevant", java.util.List.of());
    }

    @Test
    void generateToken_andExtractUsername_roundTrips() {
        String token = jwtUtil.generateToken(userDetails);

        assertThat(token).isNotBlank();
        assertThat(jwtUtil.extractUsername(token)).isEqualTo("saurav@example.com");
    }

    @Test
    void isTokenValid_returnsTrue_forMatchingUserAndUnexpiredToken() {
        String token = jwtUtil.generateToken(userDetails);

        assertThat(jwtUtil.isTokenValid(token, userDetails)).isTrue();
    }

    @Test
    void isTokenValid_returnsFalse_whenUsernameDoesNotMatch() {
        String token = jwtUtil.generateToken(userDetails);
        UserDetails differentUser = new User("someone-else@example.com", "irrelevant", java.util.List.of());

        assertThat(jwtUtil.isTokenValid(token, differentUser)).isFalse();
    }

    @Test
    void isTokenValid_returnsFalse_forAlreadyExpiredToken() {
        JwtUtil shortLivedJwtUtil = new JwtUtil(SECRET, -1000); // already expired
        String token = shortLivedJwtUtil.generateToken(userDetails);

        assertThat(shortLivedJwtUtil.isTokenValid(token, userDetails)).isFalse();
    }

    @Test
    void isTokenValid_returnsFalse_forMalformedToken() {
        assertThat(jwtUtil.isTokenValid("not-a-real-jwt", userDetails)).isFalse();
    }
}
