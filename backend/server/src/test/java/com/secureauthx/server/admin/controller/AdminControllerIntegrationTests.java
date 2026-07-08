package com.secureauthx.server.admin.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.secureauthx.server.admin.entity.SystemSetting;
import com.secureauthx.server.admin.repository.AuditLogRepository;
import com.secureauthx.server.admin.repository.SecurityIncidentRepository;
import com.secureauthx.server.admin.repository.SystemAnnouncementRepository;
import com.secureauthx.server.admin.repository.SystemSettingRepository;
import com.secureauthx.server.auth.entity.User;
import com.secureauthx.server.auth.repository.RefreshTokenRepository;
import com.secureauthx.server.auth.repository.UserRepository;
import com.secureauthx.server.authorization.entity.Role;
import com.secureauthx.server.authorization.entity.UserRole;
import com.secureauthx.server.authorization.repository.RoleRepository;
import com.secureauthx.server.authorization.repository.UserRoleRepository;
import com.secureauthx.server.oauth.repository.AuthorizationCodeRepository;
import com.secureauthx.server.organization.repository.OrganizationMemberRepository;
import com.secureauthx.server.organization.repository.OrganizationRepository;
import com.secureauthx.server.sessions.repository.SessionRepository;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(properties = {
        "management.health.redis.enabled=false"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql("/seed-roles.sql")
class AdminControllerIntegrationTests {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private RoleRepository roleRepository;
    @Autowired private UserRoleRepository userRoleRepository;
    @Autowired private SessionRepository sessionRepository;
    @Autowired private RefreshTokenRepository refreshTokenRepository;
    @Autowired private AuthorizationCodeRepository authorizationCodeRepository;
    @Autowired private OrganizationRepository organizationRepository;
    @Autowired private OrganizationMemberRepository organizationMemberRepository;
    @Autowired private AuditLogRepository auditLogRepository;
    @Autowired private SystemAnnouncementRepository announcementRepository;
    @Autowired private SystemSettingRepository settingRepository;
    @Autowired private SecurityIncidentRepository incidentRepository;

    private String adminToken;
    private UUID adminId;

    @BeforeEach
    void setUp() throws Exception {
        userRoleRepository.deleteAll();
        organizationMemberRepository.deleteAll();
        organizationRepository.deleteAll();
        sessionRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        authorizationCodeRepository.deleteAll();
        auditLogRepository.deleteAll();
        announcementRepository.deleteAll();
        incidentRepository.deleteAll();
        settingRepository.deleteAll();
        userRepository.deleteAll();

        settingRepository.save(new SystemSetting("maintenance_mode", "false", "Enables maintenance mode"));
        settingRepository.save(new SystemSetting("registration_enabled", "true", "Enables registration"));
        settingRepository.save(new SystemSetting("max_sessions_per_user", "10", "Max sessions"));
        settingRepository.save(new SystemSetting("password_policy_level", "standard", "Password policy"));

        User admin = userRepository.save(
                new User("admin@example.com", passwordEncoder.encode("AdminPass!2026")));
        adminId = admin.getId();

        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseThrow(() -> new IllegalStateException("ADMIN role not found"));
        userRoleRepository.save(new UserRole(admin, adminRole));

        adminToken = loginAndGetToken("admin@example.com", "AdminPass!2026");
    }

    @Test
    void getDashboardReturnsStats() throws Exception {
        mockMvc.perform(get("/api/v1/admin/dashboard")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(1))
                .andExpect(jsonPath("$.totalOrganizations").isNumber());
    }

    @Test
    void getAuditLogsReturnsPage() throws Exception {
        mockMvc.perform(get("/api/v1/admin/audit")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getAuditLogNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/admin/audit/{id}", "00000000-0000-0000-0000-000000000000")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void createAndListAnnouncements() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/v1/admin/announcements")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "System Update", "message": "Maintenance tonight"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("System Update"))
                .andReturn();
        String annId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asText();

        mockMvc.perform(get("/api/v1/admin/announcements")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void updateAnnouncement() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/v1/admin/announcements")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "Old", "message": "Old message"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        String annId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asText();

        mockMvc.perform(patch("/api/v1/admin/announcements/{id}", annId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "Updated", "message": "New message", "severity": "WARNING"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated"))
                .andExpect(jsonPath("$.severity").value("WARNING"));
    }

    @Test
    void deleteAnnouncement() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/v1/admin/announcements")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "To Delete", "message": "bye"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        String annId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asText();

        mockMvc.perform(delete("/api/v1/admin/announcements/{id}", annId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void getAndUpdateSettings() throws Exception {
        mockMvc.perform(get("/api/v1/admin/settings")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4));

        mockMvc.perform(put("/api/v1/admin/settings/{key}", "maintenance_mode")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"settingValue": "true"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.settingValue").value("true"));
    }

    @Test
    void listAndResolveIncidents() throws Exception {
        mockMvc.perform(get("/api/v1/admin/incidents")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void nonAdminUserGets403() throws Exception {
        User regularUser = userRepository.save(
                new User("user@example.com", passwordEncoder.encode("UserPass!2026")));
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new IllegalStateException("USER role not found"));
        userRoleRepository.save(new UserRole(regularUser, userRole));
        String userToken = loginAndGetToken("user@example.com", "UserPass!2026");

        mockMvc.perform(get("/api/v1/admin/dashboard")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    private String loginAndGetToken(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s", "password": "%s"}
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("accessToken").asText();
    }
}
