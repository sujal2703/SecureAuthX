package com.secureauthx.server.developer.service;

import com.secureauthx.server.developer.dto.RateLimitRequest;
import com.secureauthx.server.developer.dto.RateLimitResponse;
import com.secureauthx.server.developer.entity.ApiRateLimit;
import com.secureauthx.server.developer.entity.DeveloperProject;
import com.secureauthx.server.developer.exception.DeveloperProjectNotFoundException;
import com.secureauthx.server.developer.repository.ApiRateLimitRepository;
import com.secureauthx.server.developer.repository.DeveloperProjectRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RateLimitService {

    private final DeveloperProjectRepository projectRepository;
    private final ApiRateLimitRepository rateLimitRepository;

    public RateLimitService(
            DeveloperProjectRepository projectRepository,
            ApiRateLimitRepository rateLimitRepository
    ) {
        this.projectRepository = projectRepository;
        this.rateLimitRepository = rateLimitRepository;
    }

    @Transactional
    public RateLimitResponse setRateLimit(UUID projectId, RateLimitRequest request, UUID userId) {
        projectRepository.findByIdAndUserId(projectId, userId)
                .orElseThrow(() -> new DeveloperProjectNotFoundException("Project not found: " + projectId));

        ApiRateLimit rateLimit = rateLimitRepository.findByProjectId(projectId)
                .orElse(new ApiRateLimit(projectId, request.requestsPerMinute(), request.requestsPerHour()));

        rateLimit.setRequestsPerMinute(request.requestsPerMinute());
        rateLimit.setRequestsPerHour(request.requestsPerHour());
        ApiRateLimit saved = rateLimitRepository.save(rateLimit);

        return RateLimitResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public RateLimitResponse getRateLimit(UUID projectId, UUID userId) {
        projectRepository.findByIdAndUserId(projectId, userId)
                .orElseThrow(() -> new DeveloperProjectNotFoundException("Project not found: " + projectId));

        ApiRateLimit rateLimit = rateLimitRepository.findByProjectId(projectId)
                .orElseThrow(() -> new DeveloperProjectNotFoundException("Rate limit not configured for project."));

        return RateLimitResponse.from(rateLimit);
    }

    @Transactional
    public void deleteRateLimit(UUID projectId, UUID userId) {
        projectRepository.findByIdAndUserId(projectId, userId)
                .orElseThrow(() -> new DeveloperProjectNotFoundException("Project not found: " + projectId));

        rateLimitRepository.deleteByProjectId(projectId);
    }
}
