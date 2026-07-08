package com.secureauthx.server.developer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.secureauthx.server.developer.dto.RateLimitRequest;
import com.secureauthx.server.developer.dto.RateLimitResponse;
import com.secureauthx.server.developer.entity.ApiRateLimit;
import com.secureauthx.server.developer.entity.DeveloperProject;
import com.secureauthx.server.developer.exception.DeveloperProjectNotFoundException;
import com.secureauthx.server.developer.repository.ApiRateLimitRepository;
import com.secureauthx.server.developer.repository.DeveloperProjectRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RateLimitServiceTests {

    @Mock private DeveloperProjectRepository projectRepository;
    @Mock private ApiRateLimitRepository rateLimitRepository;

    private RateLimitService rateLimitService;

    private UUID userId;
    private UUID projectId;
    private DeveloperProject project;

    @BeforeEach
    void setUp() {
        rateLimitService = new RateLimitService(projectRepository, rateLimitRepository);
        userId = UUID.randomUUID();
        projectId = UUID.randomUUID();
        project = new DeveloperProject(userId, "My App", null);
        setField(project, "id", projectId);
    }

    @Test
    void setRateLimitCreatesNewWhenNoneExists() {
        RateLimitRequest request = new RateLimitRequest(100, 5000);
        when(projectRepository.findByIdAndUserId(projectId, userId)).thenReturn(Optional.of(project));
        when(rateLimitRepository.findByProjectId(projectId)).thenReturn(Optional.empty());
        when(rateLimitRepository.save(any(ApiRateLimit.class))).thenAnswer(inv -> {
            ApiRateLimit rl = inv.getArgument(0);
            setField(rl, "id", UUID.randomUUID());
            return rl;
        });

        RateLimitResponse response = rateLimitService.setRateLimit(projectId, request, userId);

        assertThat(response.requestsPerMinute()).isEqualTo(100);
        assertThat(response.requestsPerHour()).isEqualTo(5000);
    }

    @Test
    void setRateLimitUpdatesExisting() {
        RateLimitRequest request = new RateLimitRequest(200, 10000);
        ApiRateLimit existing = new ApiRateLimit(projectId, 60, 1000);
        setField(existing, "id", UUID.randomUUID());

        when(projectRepository.findByIdAndUserId(projectId, userId)).thenReturn(Optional.of(project));
        when(rateLimitRepository.findByProjectId(projectId)).thenReturn(Optional.of(existing));
        when(rateLimitRepository.save(any(ApiRateLimit.class))).thenAnswer(inv -> inv.getArgument(0));

        RateLimitResponse response = rateLimitService.setRateLimit(projectId, request, userId);

        assertThat(response.requestsPerMinute()).isEqualTo(200);
        assertThat(response.requestsPerHour()).isEqualTo(10000);
    }

    @Test
    void getRateLimitReturnsConfig() {
        ApiRateLimit rateLimit = new ApiRateLimit(projectId, 60, 1000);
        setField(rateLimit, "id", UUID.randomUUID());

        when(projectRepository.findByIdAndUserId(projectId, userId)).thenReturn(Optional.of(project));
        when(rateLimitRepository.findByProjectId(projectId)).thenReturn(Optional.of(rateLimit));

        RateLimitResponse response = rateLimitService.getRateLimit(projectId, userId);

        assertThat(response.requestsPerMinute()).isEqualTo(60);
    }

    @Test
    void getRateLimitThrowsWhenNotConfigured() {
        when(projectRepository.findByIdAndUserId(projectId, userId)).thenReturn(Optional.of(project));
        when(rateLimitRepository.findByProjectId(projectId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rateLimitService.getRateLimit(projectId, userId))
                .isInstanceOf(DeveloperProjectNotFoundException.class);
    }

    @Test
    void deleteRateLimitRemovesConfig() {
        when(projectRepository.findByIdAndUserId(projectId, userId)).thenReturn(Optional.of(project));

        rateLimitService.deleteRateLimit(projectId, userId);

        verify(rateLimitRepository).deleteByProjectId(projectId);
    }

    @Test
    void deleteRateLimitThrowsWhenProjectNotFound() {
        UUID otherId = UUID.randomUUID();
        when(projectRepository.findByIdAndUserId(projectId, otherId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rateLimitService.deleteRateLimit(projectId, otherId))
                .isInstanceOf(DeveloperProjectNotFoundException.class);
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field: " + fieldName, e);
        }
    }
}
