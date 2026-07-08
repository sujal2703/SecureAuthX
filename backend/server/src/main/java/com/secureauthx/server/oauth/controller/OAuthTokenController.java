package com.secureauthx.server.oauth.controller;

import com.secureauthx.server.oauth.dto.TokenResponse;
import com.secureauthx.server.oauth.exception.OAuthException;
import com.secureauthx.server.oauth.service.OAuthAuthorizationService;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OAuthTokenController {

    private static final Logger LOGGER = LoggerFactory.getLogger(OAuthTokenController.class);

    private final OAuthAuthorizationService oauthAuthorizationService;

    public OAuthTokenController(OAuthAuthorizationService oauthAuthorizationService) {
        this.oauthAuthorizationService = oauthAuthorizationService;
    }

    @PostMapping(value = "/oauth/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> token(
            @RequestParam("grant_type") String grantType,
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "redirect_uri", required = false) String redirectUri,
            @RequestParam(value = "client_id", required = false) String clientId,
            @RequestParam(value = "client_secret", required = false) String clientSecret,
            @RequestParam(value = "code_verifier", required = false) String codeVerifier,
            @RequestParam(value = "scope", required = false) String scope
    ) {
        try {
            if ("authorization_code".equals(grantType)) {
                if (code == null || redirectUri == null || clientId == null || codeVerifier == null) {
                    return buildErrorResponse("invalid_request", "Missing required parameters for authorization_code grant.");
                }

                TokenResponse response = oauthAuthorizationService.handleAuthorizationCodeGrant(
                        code, redirectUri, clientId, clientSecret, codeVerifier
                );

                LOGGER.info("Token issued via authorization_code grant for client_id={}", clientId);
                return ResponseEntity.ok(toResponseMap(response));

            } else if ("client_credentials".equals(grantType)) {
                if (clientId == null) {
                    return buildErrorResponse("invalid_request", "Missing client_id parameter.");
                }

                TokenResponse response = oauthAuthorizationService.handleClientCredentialsGrant(clientId, clientSecret);

                LOGGER.info("Token issued via client_credentials grant for client_id={}", clientId);
                return ResponseEntity.ok(toResponseMap(response));

            } else {
                return buildErrorResponse("unsupported_grant_type", "Grant type not supported: " + grantType);
            }
        } catch (OAuthException e) {
            LOGGER.warn("OAuth token request failed: {} ({})", e.getMessage(), e.getErrorCode());
            return buildErrorResponse(e.getErrorCode(), e.getMessage());
        }
    }

    private Map<String, Object> toResponseMap(TokenResponse response) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("access_token", response.accessToken());
        map.put("token_type", response.tokenType());
        map.put("expires_in", response.expiresIn());
        if (response.refreshToken() != null) {
            map.put("refresh_token", response.refreshToken());
        }
        if (response.scope() != null) {
            map.put("scope", response.scope());
        }
        return map;
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(String error, String description) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", error);
        body.put("error_description", description);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}
