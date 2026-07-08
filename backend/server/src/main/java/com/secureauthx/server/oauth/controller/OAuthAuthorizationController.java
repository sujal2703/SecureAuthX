package com.secureauthx.server.oauth.controller;

import com.secureauthx.server.auth.entity.User;
import com.secureauthx.server.auth.repository.UserRepository;
import com.secureauthx.server.oauth.entity.AuthorizationCode;
import com.secureauthx.server.oauth.entity.OAuthClient;
import com.secureauthx.server.oauth.service.OAuthAuthorizationService;
import java.net.URI;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OAuthAuthorizationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(OAuthAuthorizationController.class);

    private final OAuthAuthorizationService oauthAuthorizationService;
    private final UserRepository userRepository;

    public OAuthAuthorizationController(
            OAuthAuthorizationService oauthAuthorizationService,
            UserRepository userRepository
    ) {
        this.oauthAuthorizationService = oauthAuthorizationService;
        this.userRepository = userRepository;
    }

    @GetMapping("/oauth/authorize")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> authorize(
            @AuthenticationPrincipal UUID userId,
            @RequestParam("client_id") String clientId,
            @RequestParam("redirect_uri") String redirectUri,
            @RequestParam("response_type") String responseType,
            @RequestParam(required = false) String scope,
            @RequestParam String state,
            @RequestParam("code_challenge") String codeChallenge,
            @RequestParam("code_challenge_method") String codeChallengeMethod,
            @RequestParam(required = false) String nonce
    ) {
        OAuthClient client = oauthAuthorizationService.validateAuthorizationRequest(
                clientId, redirectUri, responseType, codeChallenge, codeChallengeMethod
        );

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + userId));

        AuthorizationCode authCode = oauthAuthorizationService.createAuthorizationCode(
                user, client, redirectUri, codeChallenge, codeChallengeMethod,
                nonce, scope
        );

        String location = redirectUri
                + (redirectUri.contains("?") ? "&" : "?")
                + "code=" + authCode.getCode()
                + "&state=" + state;

        LOGGER.info("Authorization code issued for client_id={} user_id={}", clientId, userId);

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(location));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }
}
