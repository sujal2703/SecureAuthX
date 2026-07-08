package com.secureauthx.server.oidc.service;

import com.secureauthx.server.auth.entity.User;
import com.secureauthx.server.auth.jwt.JwtService;
import com.secureauthx.server.oidc.dto.UserInfoResponse;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OidcService {

    private final String issuer;
    private final String authorizationEndpoint;
    private final String tokenEndpoint;
    private final String jwksEndpoint;
    private final String userinfoEndpoint;
    private final RSAPublicKey rsaPublicKey;
    private final String keyId;

    public OidcService(
            @Value("${secureauthx.oidc.issuer:http://localhost:8080}") String issuer,
            JwtService jwtService
    ) {
        this.issuer = issuer;
        this.authorizationEndpoint = issuer + "/oauth/authorize";
        this.tokenEndpoint = issuer + "/oauth/token";
        this.jwksEndpoint = issuer + "/.well-known/jwks.json";
        this.userinfoEndpoint = issuer + "/connect/userinfo";
        java.security.PublicKey publicKey = jwtService.getPublicKey();
        if (!(publicKey instanceof RSAPublicKey rsa)) {
            throw new IllegalArgumentException("Public key must be an RSA key");
        }
        this.rsaPublicKey = rsa;
        this.keyId = computeKeyId(rsa);
    }

    public Map<String, Object> getDiscoveryDocument() {
        Map<String, Object> doc = new LinkedHashMap<>();
        doc.put("issuer", issuer);
        doc.put("authorization_endpoint", authorizationEndpoint);
        doc.put("token_endpoint", tokenEndpoint);
        doc.put("userinfo_endpoint", userinfoEndpoint);
        doc.put("jwks_uri", jwksEndpoint);
        doc.put("response_types_supported", List.of("code"));
        doc.put("subject_types_supported", List.of("public"));
        doc.put("id_token_signing_alg_values_supported", List.of("RS256"));
        doc.put("scopes_supported", List.of("openid", "email", "profile"));
        doc.put("claims_supported", List.of("sub", "email", "given_name", "family_name", "iss", "aud", "exp", "iat", "auth_time", "nonce"));
        doc.put("claims_parameter_supported", false);
        doc.put("request_parameter_supported", false);
        doc.put("request_uri_parameter_supported", false);
        return doc;
    }

    public Map<String, Object> getJwks() {
        Map<String, Object> jwk = new LinkedHashMap<>();
        jwk.put("kty", "RSA");
        jwk.put("n", Base64.getUrlEncoder().withoutPadding().encodeToString(rsaPublicKey.getModulus().toByteArray()));
        jwk.put("e", Base64.getUrlEncoder().withoutPadding().encodeToString(rsaPublicKey.getPublicExponent().toByteArray()));
        jwk.put("alg", "RS256");
        jwk.put("use", "sig");
        jwk.put("kid", keyId);

        Map<String, Object> jwks = new LinkedHashMap<>();
        jwks.put("keys", List.of(jwk));
        return jwks;
    }

    public UserInfoResponse buildUserInfo(User user) {
        return new UserInfoResponse(
                user.getId().toString(),
                user.getEmail(),
                null,
                null
        );
    }

    private String computeKeyId(RSAPublicKey key) {
        byte[] modulusBytes = key.getModulus().toByteArray();
        byte[] exponentBytes = key.getPublicExponent().toByteArray();
        byte[] combined = new byte[modulusBytes.length + exponentBytes.length];
        System.arraycopy(modulusBytes, 0, combined, 0, modulusBytes.length);
        System.arraycopy(exponentBytes, 0, combined, modulusBytes.length, exponentBytes.length);
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(combined);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash).substring(0, 16);
        } catch (Exception e) {
            return "default";
        }
    }
}
