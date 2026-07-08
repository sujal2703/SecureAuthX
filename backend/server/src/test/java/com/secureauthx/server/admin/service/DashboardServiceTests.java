package com.secureauthx.server.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.secureauthx.server.admin.dto.DashboardResponse;
import com.secureauthx.server.admin.repository.SecurityIncidentRepository;
import com.secureauthx.server.auth.repository.UserRepository;
import com.secureauthx.server.developer.repository.DeveloperProjectRepository;
import com.secureauthx.server.oauth.repository.OAuthClientRepository;
import com.secureauthx.server.organization.repository.OrganizationRepository;
import com.secureauthx.server.passkey.repository.PasskeyRepository;
import com.secureauthx.server.sessions.repository.SessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTests {

    @Mock private UserRepository userRepository;
    @Mock private OrganizationRepository organizationRepository;
    @Mock private OAuthClientRepository oauthClientRepository;
    @Mock private PasskeyRepository passkeyRepository;
    @Mock private SessionRepository sessionRepository;
    @Mock private SecurityIncidentRepository incidentRepository;
    @Mock private DeveloperProjectRepository developerProjectRepository;
    @Mock private AuditService auditService;

    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        dashboardService = new DashboardService(
                userRepository, organizationRepository, oauthClientRepository,
                passkeyRepository, sessionRepository, incidentRepository,
                developerProjectRepository, auditService
        );
    }

    @Test
    void getDashboardReturnsAggregatedStats() {
        when(userRepository.count()).thenReturn(10L);
        when(organizationRepository.count()).thenReturn(5L);
        when(oauthClientRepository.count()).thenReturn(3L);
        when(passkeyRepository.count()).thenReturn(20L);
        when(sessionRepository.count()).thenReturn(15L);
        when(auditService.countLogins()).thenReturn(100L);
        when(sessionRepository.countByRevokedAtIsNull()).thenReturn(8L);
        when(incidentRepository.countByResolved(false)).thenReturn(2L);
        when(developerProjectRepository.count()).thenReturn(7L);

        DashboardResponse response = dashboardService.getDashboard();

        assertThat(response.totalUsers()).isEqualTo(10);
        assertThat(response.totalOrganizations()).isEqualTo(5);
        assertThat(response.totalOAuthClients()).isEqualTo(3);
        assertThat(response.totalPasskeys()).isEqualTo(20);
        assertThat(response.totalSessions()).isEqualTo(15);
        assertThat(response.totalLoginEvents()).isEqualTo(100);
        assertThat(response.activeSessions()).isEqualTo(8);
        assertThat(response.securityIncidents()).isEqualTo(2);
        assertThat(response.developerProjects()).isEqualTo(7);
    }
}
