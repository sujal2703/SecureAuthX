package com.secureauthx.server.developer.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.secureauthx.server.auth.entity.User;
import com.secureauthx.server.auth.repository.RefreshTokenRepository;
import com.secureauthx.server.auth.repository.UserRepository;
import com.secureauthx.server.authorization.repository.UserRoleRepository;
import com.secureauthx.server.developer.repository.DeveloperApiKeyRepository;
import com.secureauthx.server.developer.repository.DeveloperProjectRepository;
import com.secureauthx.server.oauth.entity.OAuthClient;
import com.secureauthx.server.oauth.repository.AuthorizationCodeRepository;
import com.secureauthx.server.oauth.repository.OAuthClientRepository;
import com.secureauthx.server.sessions.repository.SessionRepository;
import java.util.UUID;
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
class DeveloperProjectControllerIntegrationTests {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private DeveloperProjectRepository projectRepository;
    @Autowired private DeveloperApiKeyRepository apiKeyRepository;
    @Autowired private OAuthClientRepository oauthClientRepository;
    @Autowired private SessionRepository sessionRepository;
    @Autowired private RefreshTokenRepository refreshTokenRepository;
    @Autowired private UserRoleRepository userRoleRepository;
    @Autowired private AuthorizationCodeRepository authorizationCodeRepository;

    private String accessToken;
    private UUID userId;

    @BeforeEach
    void setUp() throws Exception {
        userRoleRepository.deleteAll();
        apiKeyRepository.deleteAll();
        projectRepository.deleteAll();
        oauthClientRepository.deleteAll();
        sessionRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        authorizationCodeRepository.deleteAll();
        userRepository.deleteAll();

        User user = userRepository.save(new User("dev-user@example.com", passwordEncoder.encode("DevPass!2026")));
        userId = user.getId();

        accessToken = loginAndGetToken("dev-user@example.com", "DevPass!2026");
    }

    @Test
    void createProjectReturns201() throws Exception {
        mockMvc.perform(post("/api/v1/developer/projects")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "My Awesome App", "description": "A test project"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("My Awesome App"))
                .andExpect(jsonPath("$.description").value("A test project"))
                .andExpect(jsonPath("$.userId").value(userId.toString()));
    }

    @Test
    void listProjectsReturnsUserProjects() throws Exception {
        mockMvc.perform(post("/api/v1/developer/projects")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "App One"}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/developer/projects")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("App One"));
    }

    @Test
    void getProjectReturnsProject() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/v1/developer/projects")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "My App"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        String projectId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asText();

        mockMvc.perform(get("/api/v1/developer/projects/{projectId}", projectId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("My App"));
    }

    @Test
    void updateProjectUpdatesName() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/v1/developer/projects")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Old Name"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        String projectId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asText();

        mockMvc.perform(put("/api/v1/developer/projects/{projectId}", projectId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "New Name"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Name"));
    }

    @Test
    void deleteProjectRemovesProject() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/v1/developer/projects")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "To Delete"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        String projectId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asText();

        mockMvc.perform(delete("/api/v1/developer/projects/{projectId}", projectId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/developer/projects/{projectId}", projectId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void createApiKeyReturnsKeyWithPrefix() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/v1/developer/projects")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Key Test App"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        String projectId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asText();

        mockMvc.perform(post("/api/v1/developer/projects/{projectId}/api-keys", projectId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"label": "Production Key"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.label").value("Production Key"))
                .andExpect(jsonPath("$.plainTextKey").exists())
                .andExpect(jsonPath("$.keyPrefix").exists());
    }

    @Test
    void listApiKeysReturnsKeys() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/v1/developer/projects")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Key List App"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        String projectId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asText();

        mockMvc.perform(post("/api/v1/developer/projects/{projectId}/api-keys", projectId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"label": "My Key"}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/developer/projects/{projectId}/api-keys", projectId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].label").value("My Key"));
    }

    @Test
    void revokeApiKeyDisablesKey() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/v1/developer/projects")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Revoke App"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        String projectId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asText();

        MvcResult keyResult = mockMvc.perform(post("/api/v1/developer/projects/{projectId}/api-keys", projectId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"label": "To Revoke"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        String keyId = objectMapper.readTree(keyResult.getResponse().getContentAsString())
                .get("id").asText();

        mockMvc.perform(delete("/api/v1/developer/projects/{projectId}/api-keys/{keyId}", projectId, keyId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void rotateSecretReturnsNewSecret() throws Exception {
        OAuthClient client = new OAuthClient("test-client-" + UUID.randomUUID(), "$argon2id$oldhash", "Test", true);
        OAuthClient savedClient = oauthClientRepository.save(client);

        MvcResult createResult = mockMvc.perform(post("/api/v1/developer/projects")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Rotate App", "oauthClientId": "%s"}
                                """.formatted(savedClient.getId())))
                .andExpect(status().isCreated())
                .andReturn();
        String projectId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asText();

        mockMvc.perform(post("/api/v1/developer/projects/{projectId}/rotate-secret", projectId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newClientSecret").exists())
                .andExpect(jsonPath("$.oauthClientId").value(savedClient.getId().toString()));
    }

    @Test
    void setAndGetRateLimit() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/v1/developer/projects")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Rate Limit App"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        String projectId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asText();

        mockMvc.perform(put("/api/v1/developer/projects/{projectId}/rate-limits", projectId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"requestsPerMinute": 120, "requestsPerHour": 5000}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestsPerMinute").value(120))
                .andExpect(jsonPath("$.requestsPerHour").value(5000));

        mockMvc.perform(get("/api/v1/developer/projects/{projectId}/rate-limits", projectId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestsPerMinute").value(120));
    }

    @Test
    void deleteRateLimitRemovesConfig() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/v1/developer/projects")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Delete Rate Limit"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        String projectId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asText();

        mockMvc.perform(put("/api/v1/developer/projects/{projectId}/rate-limits", projectId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"requestsPerMinute": 1, "requestsPerHour": 1}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/v1/developer/projects/{projectId}/rate-limits", projectId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void unauthorizedRequestReturns403() throws Exception {
        mockMvc.perform(get("/api/v1/developer/projects"))
                .andExpect(status().isForbidden());
    }

    private String loginAndGetToken(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s", "password": "%s"}
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("accessToken").asText();
    }
}
