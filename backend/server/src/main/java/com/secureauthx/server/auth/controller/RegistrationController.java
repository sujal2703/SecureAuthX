package com.secureauthx.server.auth.controller;

import com.secureauthx.server.auth.dto.RegistrationRequest;
import com.secureauthx.server.auth.dto.RegistrationResponse;
import com.secureauthx.server.auth.service.RegistrationService;
import com.secureauthx.server.common.exception.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication")
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping("/register")
    @Operation(
            summary = "Register a user",
            description = "Creates a user account with an email address and password. Does not create a session or token.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "User registered"),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Validation failed",
                            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Email already registered",
                            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
                    )
            }
    )
    ResponseEntity<RegistrationResponse> register(@Valid @RequestBody RegistrationRequest request) {
        RegistrationResponse response = registrationService.register(request);
        return ResponseEntity
                .created(URI.create("/api/v1/users/" + response.id()))
                .body(response);
    }
}
