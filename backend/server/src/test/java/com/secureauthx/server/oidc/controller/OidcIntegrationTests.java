package com.secureauthx.server.oidc.controller;

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
class OidcIntegrationTests {

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
    private String adminUserId;

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
        adminUserId = admin.getId().toString();

        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseThrow(() -> new IllegalStateException("ADMIN role not found"));
        userRoleRepository.save(new UserRole(admin, adminRole));

        adminToken = loginAndGetToken("admin@example.com", "AdminPass!2026");
    }

    @Test
    void discoveryEndpointReturns200() throws Exception {
        mockMvc.perform(get("/.well-known/openid-configuration"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.issuer").isNotEmpty())
                .andExpect(jsonPath("$.authorization_endpoint").isNotEmpty())
                .andExpect(jsonPath("$.token_endpoint").isNotEmpty())
                .andExpect(jsonPath("$.userinfo_endpoint").isNotEmpty())
                .andExpect(jsonPath("$.jwks_uri").isNotEmpty())
                .andExpect(jsonPath("$.response_types_supported").isArray())
                .andExpect(jsonPath("$.subject_types_supported").isArray())
                .andExpect(jsonPath("$.id_token_signing_alg_values_supported").isArray())
                .andExpect(jsonPath("$.scopes_supported").isArray())
                .andExpect(jsonPath("$.claims_supported").isArray());
    }

    @Test
    void jwksEndpointReturns200() throws Exception {
        mockMvc.perform(get("/.well-known/jwks.json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.keys").isArray())
                .andExpect(jsonPath("$.keys[0].kty").value("RSA"))
                .andExpect(jsonPath("$.keys[0].alg").value("RS256"))
                .andExpect(jsonPath("$.keys[0].use").value("sig"))
                .andExpect(jsonPath("$.keys[0].n").isNotEmpty())
                .andExpect(jsonPath("$.keys[0].e").isNotEmpty())
                .andExpect(jsonPath("$.keys[0].kid").isNotEmpty());
    }

    @Test
    void userinfoWithValidTokenReturnsUserData() throws Exception {
        mockMvc.perform(get("/connect/userinfo")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sub").value(adminUserId))
                .andExpect(jsonPath("$.email").value("admin@example.com"));
    }

    @Test
    void userinfoWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get("/connect/userinfo"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userinfoWithInvalidTokenReturns401() throws Exception {
        mockMvc.perform(get("/connect/userinfo")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authorizationCodeFlowWithOpenIdScopeReturnsIdToken() throws Exception {
        OAuthClient client = oauthClientRepository.save(
                new OAuthClient("oidc-client", null, "OIDC Client", false));
        redirectUriRepository.save(
                new OAuthClientRedirectUri(client, "https://example.com/callback"));

        PKCEService pkceService = new PKCEService();
        String codeVerifier = pkceService.generateCodeVerifier();
        String codeChallenge = pkceService.computeS256Challenge(codeVerifier);

        MvcResult authResult = mockMvc.perform(get("/oauth/authorize")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("client_id", "oidc-client")
                        .param("redirect_uri", "https://example.com/callback")
                        .param("response_type", "code")
                        .param("scope", "openid email")
                        .param("state", "test-state")
                        .param("nonce", "test-nonce-value")
                        .param("code_challenge", codeChallenge)
                        .param("code_challenge_method", "S256"))
                .andExpect(status().isFound())
                .andReturn();

        String code = extractQueryParam(
                authResult.getResponse().getHeader("Location"), "code");

        MvcResult tokenResult = mockMvc.perform(post("/oauth/token")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("grant_type", "authorization_code")
                        .param("code", code)
                        .param("redirect_uri", "https://example.com/callback")
                        .param("client_id", "oidc-client")
                        .param("code_verifier", codeVerifier))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").isNotEmpty())
                .andExpect(jsonPath("$.token_type").value("Bearer"))
                .andExpect(jsonPath("$.expires_in").isNumber())
                .andExpect(jsonPath("$.refresh_token").isNotEmpty())
                .andExpect(jsonPath("$.id_token").isNotEmpty())
                .andExpect(jsonPath("$.scope").value("openid email"))
                .andReturn();

        JsonNode tokenJson = objectMapper.readTree(tokenResult.getResponse().getContentAsString());
        String idToken = tokenJson.get("id_token").asText();
        assertThat(idToken).isNotBlank();

        String accessToken = tokenJson.get("access_token").asText();

        mockMvc.perform(get("/connect/userinfo")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sub").value(adminUserId));
    }

    @Test
    void authorizationCodeFlowWithoutOpenIdScopeDoesNotReturnIdToken() throws Exception {
        OAuthClient client = oauthClientRepository.save(
                new OAuthClient("no-oidc-client", null, "No OIDC Client", false));
        redirectUriRepository.save(
                new OAuthClientRedirectUri(client, "https://example.com/callback"));

        PKCEService pkceService = new PKCEService();
        String codeVerifier = pkceService.generateCodeVerifier();
        String codeChallenge = pkceService.computeS256Challenge(codeVerifier);

        MvcResult authResult = mockMvc.perform(get("/oauth/authorize")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("client_id", "no-oidc-client")
                        .param("redirect_uri", "https://example.com/callback")
                        .param("response_type", "code")
                        .param("scope", "email")
                        .param("state", "test-state")
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
                        .param("client_id", "no-oidc-client")
                        .param("code_verifier", codeVerifier))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").isNotEmpty())
                .andExpect(jsonPath("$.id_token").doesNotExist());
    }

    @Test
    void nonceIsIncludedInIdToken() throws Exception {
        OAuthClient client = oauthClientRepository.save(
                new OAuthClient("nonce-client", null, "Nonce Client", false));
        redirectUriRepository.save(
                new OAuthClientRedirectUri(client, "https://example.com/callback"));

        PKCEService pkceService = new PKCEService();
        String codeVerifier = pkceService.generateCodeVerifier();
        String codeChallenge = pkceService.computeS256Challenge(codeVerifier);

        MvcResult authResult = mockMvc.perform(get("/oauth/authorize")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("client_id", "nonce-client")
                        .param("redirect_uri", "https://example.com/callback")
                        .param("response_type", "code")
                        .param("scope", "openid")
                        .param("state", "test-state")
                        .param("nonce", "my-custom-nonce")
                        .param("code_challenge", codeChallenge)
                        .param("code_challenge_method", "S256"))
                .andExpect(status().isFound())
                .andReturn();

        String code = extractQueryParam(
                authResult.getResponse().getHeader("Location"), "code");

        MvcResult tokenResult = mockMvc.perform(post("/oauth/token")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("grant_type", "authorization_code")
                        .param("code", code)
                        .param("redirect_uri", "https://example.com/callback")
                        .param("client_id", "nonce-client")
                        .param("code_verifier", codeVerifier))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id_token").isNotEmpty())
                .andReturn();

        JsonNode tokenJson = objectMapper.readTree(tokenResult.getResponse().getContentAsString());
        String idToken = tokenJson.get("id_token").asText();

        String[] parts = idToken.split("\\.");
        assertThat(parts.length).isEqualTo(3);
        String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
        assertThat(payload).contains("\"nonce\":\"my-custom-nonce\"");
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
