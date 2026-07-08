package com.secureauthx.server.passkey.controller;

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
import com.secureauthx.server.authorization.entity.Role;
import com.secureauthx.server.authorization.entity.UserRole;
import com.secureauthx.server.authorization.repository.RoleRepository;
import com.secureauthx.server.authorization.repository.UserRoleRepository;
import com.secureauthx.server.oauth.repository.AuthorizationCodeRepository;
import com.secureauthx.server.organization.repository.OrganizationMemberRepository;
import com.secureauthx.server.organization.repository.OrganizationRepository;
import com.secureauthx.server.passkey.entity.Passkey;
import com.secureauthx.server.passkey.entity.WebAuthnChallenge;
import com.secureauthx.server.passkey.repository.PasskeyRepository;
import com.secureauthx.server.passkey.repository.WebAuthnChallengeRepository;
import com.secureauthx.server.sessions.repository.SessionRepository;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.UUID;
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
        "management.health.redis.enabled=false",
        "secureauthx.webauthn.rp-id=localhost",
        "secureauthx.webauthn.rp-name=SecureAuthX",
        "secureauthx.webauthn.origin=http://localhost:3000",
        "secureauthx.webauthn.challenge-expiration-minutes=5"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql("/seed-roles.sql")
class PasskeyIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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

    @Autowired
    private AuthorizationCodeRepository authorizationCodeRepository;

    @Autowired
    private PasskeyRepository passkeyRepository;

    @Autowired
    private WebAuthnChallengeRepository challengeRepository;

    private String adminToken;
    private UUID adminId;

    @BeforeEach
    void setUp() throws Exception {
        userRoleRepository.deleteAll();
        organizationMemberRepository.deleteAll();
        organizationRepository.deleteAll();
        sessionRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        authorizationCodeRepository.deleteAll();
        passkeyRepository.deleteAll();
        challengeRepository.deleteAll();
        userRepository.deleteAll();

        User admin = userRepository.save(
                new User("admin@example.com", passwordEncoder.encode("AdminPass!2026")));
        adminId = admin.getId();

        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseThrow(() -> new IllegalStateException("ADMIN role not found"));
        userRoleRepository.save(new UserRole(admin, adminRole));

        adminToken = loginAndGetToken("admin@example.com", "AdminPass!2026");
    }

    @Test
    void registerOptionsReturnsChallenge() throws Exception {
        mockMvc.perform(post("/api/v1/passkeys/register/options")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.challenge").isNotEmpty())
                .andExpect(jsonPath("$.rp.name").value("SecureAuthX"))
                .andExpect(jsonPath("$.rp.id").value("localhost"))
                .andExpect(jsonPath("$.user.id").value(adminId.toString()))
                .andExpect(jsonPath("$.pubKeyCredParams").isArray())
                .andExpect(jsonPath("$.authenticatorSelection.residentKey").value("required"))
                .andExpect(jsonPath("$.authenticatorSelection.userVerification").value("required"));
    }

    @Test
    void authenticateOptionsReturnsChallenge() throws Exception {
        mockMvc.perform(post("/api/v1/passkeys/authenticate/options")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.challenge").isNotEmpty())
                .andExpect(jsonPath("$.rpId").value("localhost"));
    }

    @Test
    void listPasskeysRequiresAuth() throws Exception {
        mockMvc.perform(get("/api/v1/passkeys"))
                .andExpect(status().isForbidden());
    }

    @Test
    void listPasskeysReturnsEmptyForNewUser() throws Exception {
        mockMvc.perform(get("/api/v1/passkeys")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void createAndDeletePasskey() throws Exception {
        Passkey passkey = new Passkey(
                userRepository.findById(adminId).orElseThrow(),
                "test-cred-id",
                new byte[]{1, 2, 3},
                "public-key",
                null,
                "Test Device",
                false,
                "usb"
        );
        Passkey saved = passkeyRepository.save(passkey);

        mockMvc.perform(get("/api/v1/passkeys")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(delete("/api/v1/passkeys/{id}", saved.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/passkeys")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void deleteOtherUsersPasskeyReturnsNotFound() throws Exception {
        User otherUser = userRepository.save(
                new User("other@example.com", passwordEncoder.encode("OtherPass!2026")));

        Passkey passkey = new Passkey(otherUser, "other-cred-id", new byte[]{4, 5, 6},
                "public-key", null, "Other Device", false, "usb");
        Passkey saved = passkeyRepository.save(passkey);

        mockMvc.perform(delete("/api/v1/passkeys/{id}", saved.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void unauthenticatedRegisterOptionsReturnsForbidden() throws Exception {
        mockMvc.perform(post("/api/v1/passkeys/register/options"))
                .andExpect(status().isForbidden());
    }

    @Test
    void registerVerifyRejectsInvalidChallenge() throws Exception {
        String clientDataJson = "{\"type\":\"webauthn.create\",\"challenge\":\"invalid-challenge\",\"origin\":\"http://localhost:3000\"}";
        String encodedClientData = Base64.getUrlEncoder().withoutPadding().encodeToString(
                clientDataJson.getBytes(java.nio.charset.StandardCharsets.UTF_8));

        mockMvc.perform(post("/api/v1/passkeys/register/verify")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "id": "test-cred",
                                    "rawId": "test-cred",
                                    "type": "public-key",
                                    "clientDataJSON": "%s",
                                    "attestationObject": "AQAB",
                                    "publicKey": "AAAA",
                                    "publicKeyAlgorithm": "-7"
                                }
                                """.formatted(encodedClientData)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerVerifyRejectsOriginMismatch() throws Exception {
        MvcResult optionsResult = mockMvc.perform(post("/api/v1/passkeys/register/options")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode optionsJson = objectMapper.readTree(optionsResult.getResponse().getContentAsString());
        String challenge = optionsJson.get("challenge").asText();

        String clientDataJson = "{\"type\":\"webauthn.create\",\"challenge\":\"" + challenge + "\",\"origin\":\"http://evil.com\"}";
        String encodedClientData = Base64.getUrlEncoder().withoutPadding().encodeToString(
                clientDataJson.getBytes(java.nio.charset.StandardCharsets.UTF_8));

        mockMvc.perform(post("/api/v1/passkeys/register/verify")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "id": "test-cred-2",
                                    "rawId": "test-cred-2",
                                    "type": "public-key",
                                    "clientDataJSON": "%s",
                                    "attestationObject": "AQAB",
                                    "publicKey": "AAAA",
                                    "publicKeyAlgorithm": "-7"
                                }
                                """.formatted(encodedClientData)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void authenticateVerifyReturns401ForInvalidSignature() throws Exception {
        User user = userRepository.findById(adminId).orElseThrow();
        Passkey passkey = new Passkey(user, "auth-cred-id", new byte[]{1, 2, 3},
                "public-key", null, "Auth Device", false, "usb");
        passkeyRepository.save(passkey);

        WebAuthnChallenge challenge = new WebAuthnChallenge("test-auth-challenge", user, "AUTHENTICATE",
                OffsetDateTime.now().plusMinutes(5));
        challengeRepository.save(challenge);

        String clientDataJson = "{\"type\":\"webauthn.get\",\"challenge\":\"test-auth-challenge\",\"origin\":\"http://localhost:3000\"}";
        String encodedClientData = Base64.getUrlEncoder().withoutPadding().encodeToString(
                clientDataJson.getBytes(java.nio.charset.StandardCharsets.UTF_8));

        byte[] authData = new byte[37];
        System.arraycopy(sha256("localhost".getBytes(java.nio.charset.StandardCharsets.UTF_8)), 0, authData, 0, 32);
        authData[32] = 0x05;
        String encodedAuthData = Base64.getUrlEncoder().withoutPadding().encodeToString(authData);

        mockMvc.perform(post("/api/v1/passkeys/authenticate/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "id": "auth-cred-id",
                                    "rawId": "auth-cred-id",
                                    "type": "public-key",
                                    "clientDataJSON": "%s",
                                    "authenticatorData": "%s",
                                    "signature": "AAAA"
                                }
                                """.formatted(encodedClientData, encodedAuthData)))
                .andExpect(status().isUnauthorized());
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

    private byte[] sha256(byte[] data) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            return digest.digest(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
