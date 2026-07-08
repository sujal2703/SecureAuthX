package com.secureauthx.server.admin.service;

import com.secureauthx.server.admin.dto.DashboardResponse;
import com.secureauthx.server.admin.repository.SecurityIncidentRepository;
import com.secureauthx.server.auth.repository.UserRepository;
import com.secureauthx.server.developer.repository.DeveloperProjectRepository;
import com.secureauthx.server.oauth.repository.OAuthClientRepository;
import com.secureauthx.server.organization.repository.OrganizationRepository;
import com.secureauthx.server.passkey.repository.PasskeyRepository;
import com.secureauthx.server.sessions.repository.SessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final OAuthClientRepository oauthClientRepository;
    private final PasskeyRepository passkeyRepository;
    private final SessionRepository sessionRepository;
    private final SecurityIncidentRepository incidentRepository;
    private final DeveloperProjectRepository developerProjectRepository;
    private final AuditService auditService;

    public DashboardService(
            UserRepository userRepository,
            OrganizationRepository organizationRepository,
            OAuthClientRepository oauthClientRepository,
            PasskeyRepository passkeyRepository,
            SessionRepository sessionRepository,
            SecurityIncidentRepository incidentRepository,
            DeveloperProjectRepository developerProjectRepository,
            AuditService auditService
    ) {
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.oauthClientRepository = oauthClientRepository;
        this.passkeyRepository = passkeyRepository;
        this.sessionRepository = sessionRepository;
        this.incidentRepository = incidentRepository;
        this.developerProjectRepository = developerProjectRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {
        long totalUsers = userRepository.count();
        long totalOrganizations = organizationRepository.count();
        long totalOAuthClients = oauthClientRepository.count();
        long totalPasskeys = passkeyRepository.count();
        long totalSessions = sessionRepository.count();
        long totalLoginEvents = auditService.countLogins();
        long activeSessions = sessionRepository.countByRevokedAtIsNull();
        long securityIncidents = incidentRepository.countByResolved(false);
        long developerProjects = developerProjectRepository.count();

        return new DashboardResponse(
                totalUsers, totalOrganizations, totalOAuthClients, totalPasskeys,
                totalSessions, totalLoginEvents, activeSessions,
                securityIncidents, developerProjects
        );
    }
}
