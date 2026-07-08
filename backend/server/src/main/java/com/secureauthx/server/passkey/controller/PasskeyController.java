package com.secureauthx.server.passkey.controller;

import com.secureauthx.server.auth.entity.User;
import com.secureauthx.server.auth.repository.UserRepository;
import com.secureauthx.server.common.exception.ApiErrorResponse;
import com.secureauthx.server.passkey.dto.AuthenticateOptionsResponse;
import com.secureauthx.server.passkey.dto.AuthenticateVerificationRequest;
import com.secureauthx.server.passkey.dto.AuthenticateVerificationResponse;
import com.secureauthx.server.passkey.dto.PasskeyResponse;
import com.secureauthx.server.passkey.dto.RegisterOptionsResponse;
import com.secureauthx.server.passkey.dto.RegisterVerificationRequest;
import com.secureauthx.server.passkey.dto.RegisterVerificationResponse;
import com.secureauthx.server.passkey.service.WebAuthnAuthenticationService;
import com.secureauthx.server.passkey.service.PasskeyService;
import com.secureauthx.server.passkey.service.WebAuthnRegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/passkeys")
@Tag(name = "Passkeys")
public class PasskeyController {

    private final WebAuthnRegistrationService registrationService;
    private final WebAuthnAuthenticationService authenticationService;
    private final PasskeyService passkeyService;
    private final UserRepository userRepository;

    public PasskeyController(
            WebAuthnRegistrationService registrationService,
            WebAuthnAuthenticationService authenticationService,
            PasskeyService passkeyService,
            UserRepository userRepository
    ) {
        this.registrationService = registrationService;
        this.authenticationService = authenticationService;
        this.passkeyService = passkeyService;
        this.userRepository = userRepository;
    }

    @PostMapping("/register/options")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Generate WebAuthn registration options")
    ResponseEntity<RegisterOptionsResponse> registerOptions(@AuthenticationPrincipal UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found."));
        RegisterOptionsResponse options = registrationService.generateRegistrationOptions(user);
        return ResponseEntity.ok(options);
    }

    @PostMapping("/register/verify")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Verify WebAuthn registration")
    ResponseEntity<RegisterVerificationResponse> registerVerify(
            @AuthenticationPrincipal UUID userId,
            @RequestBody RegisterVerificationRequest request
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found."));
        RegisterVerificationResponse response = registrationService.verifyRegistration(user, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/authenticate/options")
    @Operation(summary = "Generate WebAuthn authentication options")
    ResponseEntity<AuthenticateOptionsResponse> authenticateOptions(
            @RequestBody(required = false) AuthenticateOptionsRequest request
    ) {
        String userHandle = request != null ? request.userHandle() : null;
        AuthenticateOptionsResponse options = authenticationService.generateAuthenticationOptions(userHandle);
        return ResponseEntity.ok(options);
    }

    @PostMapping("/authenticate/verify")
    @Operation(summary = "Verify WebAuthn authentication")
    ResponseEntity<AuthenticateVerificationResponse> authenticateVerify(
            @RequestBody AuthenticateVerificationRequest request,
            HttpServletRequest httpRequest
    ) {
        String ipAddress = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");
        AuthenticateVerificationResponse response = authenticationService.verifyAuthentication(
                request, ipAddress, userAgent
        );
        if (response.verified()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(401).body(response);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List user's passkeys")
    ResponseEntity<List<PasskeyResponse>> listPasskeys(@AuthenticationPrincipal UUID userId) {
        List<PasskeyResponse> passkeys = passkeyService.getUserPasskeys(userId);
        return ResponseEntity.ok(passkeys);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete a passkey")
    ResponseEntity<Void> deletePasskey(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id
    ) {
        passkeyService.deletePasskey(id, userId);
        return ResponseEntity.noContent().build();
    }

    record AuthenticateOptionsRequest(String userHandle) {}
}
