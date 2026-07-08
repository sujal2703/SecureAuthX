package com.secureauthx.server.oidc.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.secureauthx.server.auth.entity.User;
import com.secureauthx.server.auth.jwt.JwtService;
import com.secureauthx.server.oidc.dto.UserInfoResponse;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class OidcServiceTests {

    private final JwtService jwtService = new JwtService(15);
    private final OidcService oidcService = new OidcService("http://localhost:8080", jwtService);

    @Test
    void discoveryDocumentContainsRequiredFields() {
        Map<String, Object> doc = oidcService.getDiscoveryDocument();

        assertThat(doc).containsKey("issuer");
        assertThat(doc).containsKey("authorization_endpoint");
        assertThat(doc).containsKey("token_endpoint");
        assertThat(doc).containsKey("userinfo_endpoint");
        assertThat(doc).containsKey("jwks_uri");
        assertThat(doc).containsKey("response_types_supported");
        assertThat(doc).containsKey("subject_types_supported");
        assertThat(doc).containsKey("id_token_signing_alg_values_supported");
        assertThat(doc).containsKey("scopes_supported");
        assertThat(doc).containsKey("claims_supported");

        assertThat(doc.get("issuer")).isEqualTo("http://localhost:8080");
        assertThat(doc.get("response_types_supported")).asList().contains("code");
        assertThat(doc.get("subject_types_supported")).asList().contains("public");
        assertThat(doc.get("id_token_signing_alg_values_supported")).asList().contains("RS256");
        assertThat(doc.get("scopes_supported")).asList().contains("openid", "email", "profile");
    }

    @Test
    @SuppressWarnings("unchecked")
    void jwksContainsValidRsaPublicKey() {
        Map<String, Object> jwks = oidcService.getJwks();

        assertThat(jwks).containsKey("keys");
        var keys = (java.util.List<Map<String, Object>>) jwks.get("keys");
        assertThat(keys).hasSize(1);

        Map<String, Object> key = keys.get(0);
        assertThat(key.get("kty")).isEqualTo("RSA");
        assertThat(key.get("alg")).isEqualTo("RS256");
        assertThat(key.get("use")).isEqualTo("sig");
        assertThat(key.get("n")).isNotNull();
        assertThat(key.get("e")).isNotNull();
        assertThat(key.get("kid")).isNotNull();
    }

    @Test
    void userInfoContainsSubAndEmail() {
        User user = new User("test@example.com", "$argon2id$hash");
        try {
            var idField = user.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, UUID.randomUUID());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        UserInfoResponse response = oidcService.buildUserInfo(user);

        assertThat(response.sub()).isEqualTo(user.getId().toString());
        assertThat(response.email()).isEqualTo("test@example.com");
    }
}
