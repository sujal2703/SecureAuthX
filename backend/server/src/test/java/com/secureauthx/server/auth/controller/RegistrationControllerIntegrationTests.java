package com.secureauthx.server.auth.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.secureauthx.server.auth.entity.User;
import com.secureauthx.server.auth.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(properties = {
        "management.health.redis.enabled=false"
})
@AutoConfigureMockMvc
@Testcontainers
class RegistrationControllerIntegrationTests {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("secureauthx_test")
            .withUsername("secureauthx")
            .withPassword("secureauthx_test_password");

    private final MockMvc mockMvc;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    RegistrationControllerIntegrationTests(
            MockMvc mockMvc,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.mockMvc = mockMvc;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @DynamicPropertySource
    static void registerDataSourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Test
    void registersUserWithArgon2idPasswordHash() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content("""
                                {
                                  "email": "Developer@Example.com",
                                  "password": "S3cureExample!2026"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.startsWith("/api/v1/users/")))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.email", equalTo("developer@example.com")))
                .andExpect(jsonPath("$.createdAt", notNullValue()))
                .andExpect(jsonPath("$.password").doesNotExist());

        User user = userRepository.findByEmail("developer@example.com").orElseThrow();
        assertThat(user.getPasswordHash()).isNotEqualTo("S3cureExample!2026");
        assertThat(user.getPasswordHash()).startsWith("$argon2id$");
        assertThat(passwordEncoder.matches("S3cureExample!2026", user.getPasswordHash())).isTrue();
    }

    @Test
    void returnsConflictForDuplicateEmail() throws Exception {
        userRepository.save(new User("duplicate@example.com", passwordEncoder.encode("S3cureExample!2026")));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content("""
                                {
                                  "email": "duplicate@example.com",
                                  "password": "S3cureExample!2026"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", equalTo(409)))
                .andExpect(jsonPath("$.message", equalTo("Email is already registered.")));
    }

    @Test
    void returnsBadRequestForInvalidRequest() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content("""
                                {
                                  "email": "not-an-email",
                                  "password": "weak"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", equalTo(400)))
                .andExpect(jsonPath("$.fieldErrors.email", notNullValue()))
                .andExpect(jsonPath("$.fieldErrors.password", notNullValue()));
    }
}
