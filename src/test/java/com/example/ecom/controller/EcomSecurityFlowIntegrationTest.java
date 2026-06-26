package com.example.ecom.controller;

import com.example.ecom.data.User;
import com.example.ecom.data.UserRole;
import com.example.ecom.dto.LoginRequest;
import com.example.ecom.dto.ProductRequest;
import com.example.ecom.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Exercises the full secured business flow end-to-end:
 * admin creates a product -> customer authenticates -> customer adds it to
 * the cart -> customer checks out -> cart is cleared.
 * Also asserts that unauthenticated and under-privileged requests are rejected.
 */
@SpringBootTest
@AutoConfigureMockMvc
class EcomSecurityFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String ADMIN_EMAIL = "flow-admin@example.com";
    private static final String ADMIN_PASSWORD = "AdminPass123";

    @BeforeEach
    void seedAdminUser() {
        if (userRepository.findByEmail(ADMIN_EMAIL).isPresent()) {
            return;
        }
        User admin = new User();
        admin.setFirstname("Admin");
        admin.setLastname("User");
        admin.setEmail(ADMIN_EMAIL);
        admin.setPhone("8888888888");
        admin.setPassword(passwordEncoder.encode(ADMIN_PASSWORD));
        admin.setRole(UserRole.ADMIN);
        userRepository.save(admin);
    }

    private String loginAndGetToken(String email, String password) throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword(password);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("token").asText();
    }

    @Test
    void productEndpoints_rejectRequests_withNoToken() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void productCreation_isForbidden_forCustomerRole() throws Exception {
        // Register a plain customer
        var registerRequest = new com.example.ecom.dto.RegisterRequest();
        registerRequest.setFirstName("Plain");
        registerRequest.setLastName("Customer");
        registerRequest.setEmail("plain-customer@example.com");
        registerRequest.setPhone("7777777777");
        registerRequest.setPassword("CustomerPass123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        String customerToken = loginAndGetToken("plain-customer@example.com", "CustomerPass123");

        ProductRequest productRequest = new ProductRequest();
        productRequest.setName("Should Not Be Created");
        productRequest.setPrice(BigDecimal.TEN);
        productRequest.setStockQuantity(5);

        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void fullPurchaseFlow_adminCreatesProduct_customerOrdersIt() throws Exception {
        String adminToken = loginAndGetToken(ADMIN_EMAIL, ADMIN_PASSWORD);

        // 1. Admin creates a product
        ProductRequest productRequest = new ProductRequest();
        productRequest.setName("Mechanical Keyboard");
        productRequest.setDescription("Hot-swappable, tactile switches");
        productRequest.setPrice(BigDecimal.valueOf(3000));
        productRequest.setStockQuantity(10);
        productRequest.setCategory("Electronics");

        MvcResult createResult = mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        long productId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asLong();

        // 2. Register and log in as a customer
        var registerRequest = new com.example.ecom.dto.RegisterRequest();
        registerRequest.setFirstName("Saurav");
        registerRequest.setLastName("Buyer");
        registerRequest.setEmail("buyer@example.com");
        registerRequest.setPhone("6666666666");
        registerRequest.setPassword("BuyerPass123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        String customerToken = loginAndGetToken("buyer@example.com", "BuyerPass123");

        // 3. Customer adds the product to their cart
        String cartBody = String.format("{\"productId\": %d, \"quantity\": 2}", productId);
        mockMvc.perform(post("/api/cart")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cartBody))
                .andExpect(status().isCreated());

        // 4. Cart reflects the item
        mockMvc.perform(get("/api/cart")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].quantity").value(2));

        // 5. Customer checks out
        MvcResult orderResult = mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.totalAmount").value(6000.0))
                .andReturn();

        assertThat(orderResult.getResponse().getContentAsString()).contains("CONFIRMED");

        // 6. Cart is now empty after checkout
        mockMvc.perform(get("/api/cart")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}
