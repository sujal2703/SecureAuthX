package com.secureauthx.server.developer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.secureauthx.server.developer.dto.UsageAnalyticsResponse;
import com.secureauthx.server.developer.entity.DeveloperApiUsage;
import com.secureauthx.server.developer.entity.DeveloperProject;
import com.secureauthx.server.developer.exception.DeveloperProjectNotFoundException;
import com.secureauthx.server.developer.repository.DeveloperApiUsageRepository;
import com.secureauthx.server.developer.repository.DeveloperProjectRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UsageAnalyticsServiceTests {

    @Mock private DeveloperProjectRepository projectRepository;
    @Mock private DeveloperApiUsageRepository usageRepository;

    private UsageAnalyticsService usageAnalyticsService;

    private UUID userId;
    private UUID projectId;
    private DeveloperProject project;

    @BeforeEach
    void setUp() {
        usageAnalyticsService = new UsageAnalyticsService(projectRepository, usageRepository);
        userId = UUID.randomUUID();
        projectId = UUID.randomUUID();
        project = new DeveloperProject(userId, "My App", null);
        setField(project, "id", projectId);
    }

    @Test
    void getUsageReturnsDataForDateRange() {
        LocalDate start = LocalDate.of(2026, 7, 1);
        LocalDate end = LocalDate.of(2026, 7, 7);
        DeveloperApiUsage usage = new DeveloperApiUsage(projectId, LocalDate.of(2026, 7, 5));
        setField(usage, "id", UUID.randomUUID());

        when(projectRepository.findByIdAndUserId(projectId, userId)).thenReturn(Optional.of(project));
        when(usageRepository.findByProjectIdAndDateBetweenOrderByDateAsc(projectId, start, end))
                .thenReturn(List.of(usage));

        List<UsageAnalyticsResponse> result = usageAnalyticsService.getUsage(projectId, start, end, userId);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().date()).isEqualTo(LocalDate.of(2026, 7, 5));
    }

    @Test
    void getUsageThrowsWhenProjectNotFound() {
        when(projectRepository.findByIdAndUserId(projectId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usageAnalyticsService.getUsage(
                projectId, LocalDate.now(), LocalDate.now(), userId))
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
