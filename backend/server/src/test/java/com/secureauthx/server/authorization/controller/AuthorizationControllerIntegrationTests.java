package com.secureauthx.server.authorization.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.secureauthx.server.auth.entity.User;
import com.secureauthx.server.auth.repository.RefreshTokenRepository;
import com.secureauthx.server.auth.repository.UserRepository;
import com.secureauthx.server.authorization.entity.Permission;
import com.secureauthx.server.authorization.entity.Role;
import com.secureauthx.server.authorization.entity.RolePermission;
import com.secureauthx.server.authorization.entity.UserRole;
import com.secureauthx.server.authorization.repository.PermissionRepository;
import com.secureauthx.server.authorization.repository.RolePermissionRepository;
import com.secureauthx.server.authorization.repository.RoleRepository;
import com.secureauthx.server.authorization.repository.UserRoleRepository;
import com.secureauthx.server.sessions.repository.SessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(properties = {
        "management.health.redis.enabled=false"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthorizationControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private RolePermissionRepository rolePermissionRepository;

    private String userAccessToken;
    private String adminAccessToken;

    @BeforeEach
    void setUp() throws Exception {
        rolePermissionRepository.deleteAll();
        permissionRepository.deleteAll();
        sessionRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRoleRepository.deleteAll();
        roleRepository.deleteAll();
        userRepository.deleteAll();

        User user = userRepository.save(new User("user@example.com", passwordEncoder.encode("UserPass!2026")));
        User admin = userRepository.save(new User("admin@example.com", passwordEncoder.encode("AdminPass!2026")));

        Role userRole = roleRepository.save(new Role("USER", "Standard user"));
        Role adminRole = roleRepository.save(new Role("ADMIN", "Administrator"));

        Permission userRead = permissionRepository.save(new Permission("USER_READ", "View user details"));
        Permission userWrite = permissionRepository.save(new Permission("USER_WRITE", "Create or update users"));
        Permission sessionRead = permissionRepository.save(new Permission("SESSION_READ", "View session details"));
        Permission sessionRevoke = permissionRepository.save(new Permission("SESSION_REVOKE", "Revoke sessions"));
        Permission roleRead = permissionRepository.save(new Permission("ROLE_READ", "View role details"));
        Permission roleWrite = permissionRepository.save(new Permission("ROLE_WRITE", "Create or update roles"));

        userRoleRepository.save(new UserRole(user, userRole));
        userRoleRepository.save(new UserRole(admin, adminRole));

        rolePermissionRepository.save(new RolePermission(adminRole, userRead));
        rolePermissionRepository.save(new RolePermission(adminRole, userWrite));
        rolePermissionRepository.save(new RolePermission(adminRole, sessionRead));
        rolePermissionRepository.save(new RolePermission(adminRole, sessionRevoke));
        rolePermissionRepository.save(new RolePermission(adminRole, roleRead));
        rolePermissionRepository.save(new RolePermission(adminRole, roleWrite));
        rolePermissionRepository.save(new RolePermission(userRole, sessionRead));
        rolePermissionRepository.save(new RolePermission(userRole, sessionRevoke));
        rolePermissionRepository.save(new RolePermission(userRole, roleRead));

        userAccessToken = loginAndGetToken("user@example.com", "UserPass!2026");
        adminAccessToken = loginAndGetToken("admin@example.com", "AdminPass!2026");
    }

    @Test
    void userCanListRoles() throws Exception {
        mockMvc.perform(get("/api/v1/roles")
                        .header("Authorization", "Bearer " + userAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[?(@.name=='USER')]").exists())
                .andExpect(jsonPath("$[?(@.name=='ADMIN')]").exists());
    }

    @Test
    void userCanListPermissions() throws Exception {
        mockMvc.perform(get("/api/v1/permissions")
                        .header("Authorization", "Bearer " + adminAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(6)))
                .andExpect(jsonPath("$[?(@.name=='USER_READ')]").exists())
                .andExpect(jsonPath("$[?(@.name=='ROLE_WRITE')]").exists());
    }

    @Test
    void unauthenticatedRequestToRolesReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/roles"))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedRequestToPermissionsReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/permissions"))
                .andExpect(status().isForbidden());
    }

    @Test
    void rolesResponseDoesNotExposeInternalMappings() throws Exception {
        String responseJson = mockMvc.perform(get("/api/v1/roles")
                        .header("Authorization", "Bearer " + userAccessToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode roles = objectMapper.readTree(responseJson);
        for (JsonNode role : roles) {
            org.assertj.core.api.Assertions.assertThat(role.has("createdAt")).isFalse();
            org.assertj.core.api.Assertions.assertThat(role.has("updatedAt")).isFalse();
            org.assertj.core.api.Assertions.assertThat(role.has("permissions")).isFalse();
        }
    }

    private String loginAndGetToken(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s", "password": "%s"}
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        String token = json.get("accessToken").asText();
        assertThat(token).isNotBlank();
        return token;
    }
}
