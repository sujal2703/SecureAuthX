package com.secureauthx.server.developer.service;

import com.secureauthx.server.developer.dto.UsageAnalyticsResponse;
import com.secureauthx.server.developer.entity.DeveloperProject;
import com.secureauthx.server.developer.exception.DeveloperProjectNotFoundException;
import com.secureauthx.server.developer.repository.DeveloperApiUsageRepository;
import com.secureauthx.server.developer.repository.DeveloperProjectRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsageAnalyticsService {

    private final DeveloperProjectRepository projectRepository;
    private final DeveloperApiUsageRepository usageRepository;

    public UsageAnalyticsService(
            DeveloperProjectRepository projectRepository,
            DeveloperApiUsageRepository usageRepository
    ) {
        this.projectRepository = projectRepository;
        this.usageRepository = usageRepository;
    }

    @Transactional(readOnly = true)
    public List<UsageAnalyticsResponse> getUsage(UUID projectId, LocalDate startDate, LocalDate endDate, UUID userId) {
        projectRepository.findByIdAndUserId(projectId, userId)
                .orElseThrow(() -> new DeveloperProjectNotFoundException("Project not found: " + projectId));

        return usageRepository.findByProjectIdAndDateBetweenOrderByDateAsc(projectId, startDate, endDate)
                .stream()
                .map(u -> new UsageAnalyticsResponse(
                        u.getId(),
                        u.getProjectId(),
                        u.getDate(),
                        u.getRequestCount(),
                        u.getSuccessCount(),
                        u.getFailureCount(),
                        u.getAvgLatencyMs(),
                        u.getLastRequestAt(),
                        u.getTokenExchanges(),
                        u.getUserinfoRequests()
                ))
                .toList();
    }
}
