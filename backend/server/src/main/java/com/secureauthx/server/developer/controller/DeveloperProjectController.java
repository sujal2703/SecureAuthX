package com.secureauthx.server.developer.controller;

import com.secureauthx.server.developer.dto.ApiKeyResponse;
import com.secureauthx.server.developer.dto.CreateApiKeyRequest;
import com.secureauthx.server.developer.dto.CreateApiKeyResponse;
import com.secureauthx.server.developer.dto.ProjectCreateRequest;
import com.secureauthx.server.developer.dto.ProjectResponse;
import com.secureauthx.server.developer.dto.ProjectUpdateRequest;
import com.secureauthx.server.developer.dto.RateLimitRequest;
import com.secureauthx.server.developer.dto.RateLimitResponse;
import com.secureauthx.server.developer.dto.RotateSecretResponse;
import com.secureauthx.server.developer.dto.UsageAnalyticsResponse;
import com.secureauthx.server.developer.service.ApiKeyService;
import com.secureauthx.server.developer.service.DeveloperPortalService;
import com.secureauthx.server.developer.service.RateLimitService;
import com.secureauthx.server.developer.service.SecretRotationService;
import com.secureauthx.server.developer.service.UsageAnalyticsService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/developer")
public class DeveloperProjectController {

    private final DeveloperPortalService developerPortalService;
    private final ApiKeyService apiKeyService;
    private final SecretRotationService secretRotationService;
    private final UsageAnalyticsService usageAnalyticsService;
    private final RateLimitService rateLimitService;

    public DeveloperProjectController(
            DeveloperPortalService developerPortalService,
            ApiKeyService apiKeyService,
            SecretRotationService secretRotationService,
            UsageAnalyticsService usageAnalyticsService,
            RateLimitService rateLimitService
    ) {
        this.developerPortalService = developerPortalService;
        this.apiKeyService = apiKeyService;
        this.secretRotationService = secretRotationService;
        this.usageAnalyticsService = usageAnalyticsService;
        this.rateLimitService = rateLimitService;
    }

    private UUID extractUserId(Principal principal) {
        return UUID.fromString(principal.getName());
    }

    @PostMapping("/projects")
    public ResponseEntity<ProjectResponse> createProject(
            @Valid @RequestBody ProjectCreateRequest request,
            Principal principal
    ) {
        ProjectResponse response = developerPortalService.createProject(request, extractUserId(principal));
        return ResponseEntity.status(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }

    @GetMapping("/projects")
    public ResponseEntity<List<ProjectResponse>> listProjects(Principal principal) {
        List<ProjectResponse> projects = developerPortalService.listProjects(extractUserId(principal));
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/projects/{projectId}")
    public ResponseEntity<ProjectResponse> getProject(
            @PathVariable UUID projectId,
            Principal principal
    ) {
        ProjectResponse project = developerPortalService.getProject(projectId, extractUserId(principal));
        return ResponseEntity.ok(project);
    }

    @PutMapping("/projects/{projectId}")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable UUID projectId,
            @Valid @RequestBody ProjectUpdateRequest request,
            Principal principal
    ) {
        ProjectResponse project = developerPortalService.updateProject(projectId, request, extractUserId(principal));
        return ResponseEntity.ok(project);
    }

    @DeleteMapping("/projects/{projectId}")
    public ResponseEntity<Void> deleteProject(
            @PathVariable UUID projectId,
            Principal principal
    ) {
        developerPortalService.deleteProject(projectId, extractUserId(principal));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/projects/{projectId}/api-keys")
    public ResponseEntity<CreateApiKeyResponse> createApiKey(
            @PathVariable UUID projectId,
            @Valid @RequestBody CreateApiKeyRequest request,
            Principal principal
    ) {
        CreateApiKeyResponse response = apiKeyService.createApiKey(projectId, request, extractUserId(principal));
        return ResponseEntity.status(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }

    @GetMapping("/projects/{projectId}/api-keys")
    public ResponseEntity<List<ApiKeyResponse>> listApiKeys(
            @PathVariable UUID projectId,
            Principal principal
    ) {
        List<ApiKeyResponse> keys = apiKeyService.listApiKeys(projectId, extractUserId(principal));
        return ResponseEntity.ok(keys);
    }

    @DeleteMapping("/projects/{projectId}/api-keys/{keyId}")
    public ResponseEntity<Void> revokeApiKey(
            @PathVariable UUID projectId,
            @PathVariable UUID keyId,
            Principal principal
    ) {
        apiKeyService.revokeApiKey(projectId, keyId, extractUserId(principal));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/projects/{projectId}/rotate-secret")
    public ResponseEntity<RotateSecretResponse> rotateSecret(
            @PathVariable UUID projectId,
            Principal principal
    ) {
        RotateSecretResponse response = secretRotationService.rotateSecret(projectId, extractUserId(principal));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/projects/{projectId}/usage")
    public ResponseEntity<List<UsageAnalyticsResponse>> getUsage(
            @PathVariable UUID projectId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            Principal principal
    ) {
        List<UsageAnalyticsResponse> usage = usageAnalyticsService.getUsage(
                projectId, startDate, endDate, extractUserId(principal));
        return ResponseEntity.ok(usage);
    }

    @PutMapping("/projects/{projectId}/rate-limits")
    public ResponseEntity<RateLimitResponse> setRateLimit(
            @PathVariable UUID projectId,
            @Valid @RequestBody RateLimitRequest request,
            Principal principal
    ) {
        RateLimitResponse response = rateLimitService.setRateLimit(projectId, request, extractUserId(principal));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/projects/{projectId}/rate-limits")
    public ResponseEntity<RateLimitResponse> getRateLimit(
            @PathVariable UUID projectId,
            Principal principal
    ) {
        RateLimitResponse response = rateLimitService.getRateLimit(projectId, extractUserId(principal));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/projects/{projectId}/rate-limits")
    public ResponseEntity<Void> deleteRateLimit(
            @PathVariable UUID projectId,
            Principal principal
    ) {
        rateLimitService.deleteRateLimit(projectId, extractUserId(principal));
        return ResponseEntity.noContent().build();
    }
}
