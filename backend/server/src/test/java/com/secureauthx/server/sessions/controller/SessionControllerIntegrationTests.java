package com.secureauthx.server.sessions.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.secureauthx.server.auth.entity.User;
import com.secureauthx.server.auth.repository.RefreshTokenRepository;
import com.secureauthx.server.auth.repository.UserRepository;
import com.secureauthx.server.sessions.repository.SessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(properties = {
        "management.health.redis.enabled=false"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SessionControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @BeforeEach
    void setUp() {
        sessionRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
        userRepository.save(new User("session-test@example.com", passwordEncoder.encode("S3cureExample!2026")));
    }

    @Test
    void loginCreatesSession() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "session-test@example.com",
                                  "password": "S3cureExample!2026"
                                }
                                """)
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120")
                        .with(request -> {
                            request.setRemoteAddr("192.168.1.100");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andReturn();

        String loginResponse = loginResult.getResponse().getContentAsString();
        JsonNode loginJson = objectMapper.readTree(loginResponse);
        String accessToken = loginJson.get("accessToken").asText();

        mockMvc.perform(get("/api/v1/sessions")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].browser").value("Chrome"))
                .andExpect(jsonPath("$[0].operatingSystem").value("Windows"))
                .andExpect(jsonPath("$[0].deviceName").value("Windows PC"))
                .andExpect(jsonPath("$[0].ipAddress").value("192.168.1.100"))
                .andExpect(jsonPath("$[0].isRevoked").value(false));
    }

    @Test
    void listSessionsRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/sessions"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getCurrentSessionReturnsFirstSession() throws Exception {
        String accessToken = loginAndGetAccessToken();

        mockMvc.perform(get("/api/v1/sessions/current")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.isRevoked").value(false));
    }

    @Test
    void revokeSessionReturnsNoContent() throws Exception {
        String accessToken = loginAndGetAccessToken();

        MvcResult listResult = mockMvc.perform(get("/api/v1/sessions")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode sessions = objectMapper.readTree(listResult.getResponse().getContentAsString());
        String sessionId = sessions.get(0).get("id").asText();

        mockMvc.perform(delete("/api/v1/sessions/{sessionId}", sessionId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void revokeNonExistentSessionReturnsNotFound() throws Exception {
        String accessToken = loginAndGetAccessToken();

        mockMvc.perform(delete("/api/v1/sessions/{sessionId}", "00000000-0000-0000-0000-000000000000")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void revokeAllSessionsReturnsNoContent() throws Exception {
        String accessToken = loginAndGetAccessToken();

        mockMvc.perform(delete("/api/v1/sessions/all")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/sessions")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void revokeCurrentSessionReturnsNoContent() throws Exception {
        String accessToken = loginAndGetAccessToken();

        mockMvc.perform(delete("/api/v1/sessions/current")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void multipleLoginsCreateMultipleSessions() throws Exception {
        String accessToken = loginAndGetAccessToken();
        loginAndGetAccessToken();

        mockMvc.perform(get("/api/v1/sessions")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    private String loginAndGetAccessToken() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "session-test@example.com",
                                  "password": "S3cureExample!2026"
                                }
                                """)
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120")
                        .with(request -> {
                            request.setRemoteAddr("192.168.1.100");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andReturn();

        String loginResponse = loginResult.getResponse().getContentAsString();
        JsonNode loginJson = objectMapper.readTree(loginResponse);
        return loginJson.get("accessToken").asText();
    }
}
