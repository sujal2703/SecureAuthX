package com.secureauthx.server.oauth.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.secureauthx.server.auth.entity.User;
import com.secureauthx.server.auth.repository.RefreshTokenRepository;
import com.secureauthx.server.auth.repository.UserRepository;
import com.secureauthx.server.authorization.entity.Role;
import com.secureauthx.server.authorization.entity.UserRole;
import com.secureauthx.server.authorization.repository.RoleRepository;
import com.secureauthx.server.authorization.repository.UserRoleRepository;
import com.secureauthx.server.oauth.entity.OAuthClient;
import com.secureauthx.server.oauth.entity.OAuthClientRedirectUri;
import com.secureauthx.server.oauth.repository.AuthorizationCodeRepository;
import com.secureauthx.server.oauth.repository.OAuthClientRedirectUriRepository;
import com.secureauthx.server.oauth.repository.OAuthClientRepository;
import com.secureauthx.server.oauth.service.PKCEService;
import com.secureauthx.server.organization.repository.OrganizationMemberRepository;
import com.secureauthx.server.organization.repository.OrganizationRepository;
import com.secureauthx.server.sessions.repository.SessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(properties = {
        "management.health.redis.enabled=false"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql("/seed-roles.sql")
class OAuthIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private OAuthClientRepository oauthClientRepository;

    @Autowired
    private OAuthClientRedirectUriRepository redirectUriRepository;

    @Autowired
    private AuthorizationCodeRepository authorizationCodeRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private OrganizationMemberRepository organizationMemberRepository;

    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        userRoleRepository.deleteAll();
        organizationMemberRepository.deleteAll();
        organizationRepository.deleteAll();
        sessionRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        authorizationCodeRepository.deleteAll();
        redirectUriRepository.deleteAll();
        oauthClientRepository.deleteAll();
        userRepository.deleteAll();

        User admin = userRepository.save(
                new User("admin@example.com", passwordEncoder.encode("AdminPass!2026")));

        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseThrow(() -> new IllegalStateException("ADMIN role not found"));
        userRoleRepository.save(new UserRole(admin, adminRole));

        adminToken = loginAndGetToken("admin@example.com", "AdminPass!2026");
    }

    @Test
    void createAndRetrieveOAuthClient() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/v1/oauth/clients")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "clientId": "test-client",
                                  "clientName": "Test Client",
                                  "confidential": true,
                                  "clientSecret": "test-secret",
                                  "redirectUris": ["https://example.com/callback"]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clientId").value("test-client"))
                .andReturn();

        JsonNode createJson = objectMapper.readTree(createResult.getResponse().getContentAsString());
        String clientId = createJson.get("id").asText();

        mockMvc.perform(get("/api/v1/oauth/clients/{id}", clientId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientId").value("test-client"))
                .andExpect(jsonPath("$.redirectUris[0]").value("https://example.com/callback"));
    }

    @Test
    void listClientsReturnsAllClients() throws Exception {
        oauthClientRepository.deleteAll();

        mockMvc.perform(post("/api/v1/oauth/clients")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "clientId": "client-a",
                                  "clientName": "Client A",
                                  "confidential": false,
                                  "redirectUris": ["https://a.example.com/callback"]
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/oauth/clients")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "clientId": "client-b",
                                  "clientName": "Client B",
                                  "confidential": false,
                                  "redirectUris": ["https://b.example.com/callback"]
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/oauth/clients")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void unauthenticatedClientManagementReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/oauth/clients"))
                .andExpect(status().isForbidden());
    }

    @Test
    void nonAdminCannotCreateClient() throws Exception {
        User regularUser = userRepository.save(
                new User("user@example.com", passwordEncoder.encode("UserPass!2026")));
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new IllegalStateException("USER role not found"));
        userRoleRepository.save(new UserRole(regularUser, userRole));
        String userToken = loginAndGetToken("user@example.com", "UserPass!2026");

        mockMvc.perform(post("/api/v1/oauth/clients")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "clientId": "my-client",
                                  "clientName": "My Client",
                                  "confidential": false,
                                  "redirectUris": ["https://example.com/callback"]
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void authorizationCodeFlowSucceeds() throws Exception {
        OAuthClient client = oauthClientRepository.save(
                new OAuthClient("my-client", null, "My Client", false));
        redirectUriRepository.save(
                new OAuthClientRedirectUri(client, "https://example.com/callback"));

        PKCEService pkceService = new PKCEService();
        String codeVerifier = pkceService.generateCodeVerifier();
        String codeChallenge = pkceService.computeS256Challenge(codeVerifier);

        MvcResult authResult = mockMvc.perform(get("/oauth/authorize")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("client_id", "my-client")
                        .param("redirect_uri", "https://example.com/callback")
                        .param("response_type", "code")
                        .param("scope", "openid")
                        .param("state", "test-state")
                        .param("code_challenge", codeChallenge)
                        .param("code_challenge_method", "S256"))
                .andExpect(status().isFound())
                .andReturn();

        String location = authResult.getResponse().getHeader("Location");
        assertThat(location).isNotNull();
        assertThat(location).startsWith("https://example.com/callback");
        assertThat(location).contains("code=");
        assertThat(location).contains("state=test-state");

        String code = extractQueryParam(location, "code");

        MvcResult tokenResult = mockMvc.perform(post("/oauth/token")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("grant_type", "authorization_code")
                        .param("code", code)
                        .param("redirect_uri", "https://example.com/callback")
                        .param("client_id", "my-client")
                        .param("code_verifier", codeVerifier))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").isNotEmpty())
                .andExpect(jsonPath("$.token_type").value("Bearer"))
                .andExpect(jsonPath("$.expires_in").isNumber())
                .andExpect(jsonPath("$.refresh_token").isNotEmpty())
                .andReturn();

        JsonNode tokenJson = objectMapper.readTree(tokenResult.getResponse().getContentAsString());
        String accessToken = tokenJson.get("access_token").asText();

        mockMvc.perform(get("/api/v1/organizations")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    void authorizationCodeCannotBeReused() throws Exception {
        OAuthClient client = oauthClientRepository.save(
                new OAuthClient("reuse-client", null, "Reuse Client", false));
        redirectUriRepository.save(
                new OAuthClientRedirectUri(client, "https://example.com/callback"));

        PKCEService pkceService = new PKCEService();
        String codeVerifier = pkceService.generateCodeVerifier();
        String codeChallenge = pkceService.computeS256Challenge(codeVerifier);

        MvcResult authResult = mockMvc.perform(get("/oauth/authorize")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("client_id", "reuse-client")
                        .param("redirect_uri", "https://example.com/callback")
                        .param("response_type", "code")
                        .param("state", "state-1")
                        .param("code_challenge", codeChallenge)
                        .param("code_challenge_method", "S256"))
                .andExpect(status().isFound())
                .andReturn();

        String code = extractQueryParam(
                authResult.getResponse().getHeader("Location"), "code");

        mockMvc.perform(post("/oauth/token")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("grant_type", "authorization_code")
                        .param("code", code)
                        .param("redirect_uri", "https://example.com/callback")
                        .param("client_id", "reuse-client")
                        .param("code_verifier", codeVerifier))
                .andExpect(status().isOk());

        mockMvc.perform(post("/oauth/token")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("grant_type", "authorization_code")
                        .param("code", code)
                        .param("redirect_uri", "https://example.com/callback")
                        .param("client_id", "reuse-client")
                        .param("code_verifier", codeVerifier))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("invalid_grant"));
    }

    @Test
    void clientCredentialsGrantSucceeds() throws Exception {
        OAuthClient client = oauthClientRepository.save(
                new OAuthClient("cc-client", passwordEncoder.encode("cc-secret"), "CC Client", true));

        MvcResult tokenResult = mockMvc.perform(post("/oauth/token")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("grant_type", "client_credentials")
                        .param("client_id", "cc-client")
                        .param("client_secret", "cc-secret"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").isNotEmpty())
                .andExpect(jsonPath("$.token_type").value("Bearer"))
                .andExpect(jsonPath("$.expires_in").isNumber())
                .andReturn();

        JsonNode tokenJson = objectMapper.readTree(tokenResult.getResponse().getContentAsString());
        assertThat(tokenJson.has("refresh_token")).isFalse();
    }

    @Test
    void clientCredentialsRejectsInvalidSecret() throws Exception {
        oauthClientRepository.save(
                new OAuthClient("cc-client", passwordEncoder.encode("cc-secret"), "CC Client", true));

        mockMvc.perform(post("/oauth/token")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("grant_type", "client_credentials")
                        .param("client_id", "cc-client")
                        .param("client_secret", "wrong-secret"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("invalid_client"));
    }

    @Test
    void redirectUriMismatchIsRejected() throws Exception {
        OAuthClient client = oauthClientRepository.save(
                new OAuthClient("uri-client", null, "URI Client", false));
        redirectUriRepository.save(
                new OAuthClientRedirectUri(client, "https://allowed.com/callback"));

        mockMvc.perform(get("/oauth/authorize")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("client_id", "uri-client")
                        .param("redirect_uri", "https://evil.com/callback")
                        .param("response_type", "code")
                        .param("state", "state-1")
                        .param("code_challenge", "E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM")
                        .param("code_challenge_method", "S256"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.error").value("invalid_client"));
    }

    @Test
    void unsupportedGrantTypeReturnsError() throws Exception {
        mockMvc.perform(post("/oauth/token")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("grant_type", "implicit"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("unsupported_grant_type"));
    }

    @Test
    void authorizationCodeRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/oauth/authorize")
                        .param("client_id", "any-client")
                        .param("redirect_uri", "https://example.com/callback")
                        .param("response_type", "code")
                        .param("state", "state-1")
                        .param("code_challenge", "test-challenge")
                        .param("code_challenge_method", "S256"))
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

    private String extractQueryParam(String url, String param) {
        if (url == null) return null;
        String query = url.contains("?") ? url.substring(url.indexOf("?") + 1) : url;
        for (String pair : query.split("&")) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue[0].equals(param) && keyValue.length > 1) {
                return keyValue[1];
            }
        }
        return null;
    }
}
