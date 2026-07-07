package com.secureauthx.server.organization.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.secureauthx.server.auth.entity.User;
import com.secureauthx.server.auth.repository.RefreshTokenRepository;
import com.secureauthx.server.authorization.repository.UserRoleRepository;
import java.util.UUID;
import com.secureauthx.server.auth.repository.UserRepository;
import com.secureauthx.server.organization.entity.Organization;
import com.secureauthx.server.organization.entity.OrganizationMember;
import com.secureauthx.server.organization.entity.OrganizationRole;
import com.secureauthx.server.organization.repository.OrganizationMemberRepository;
import com.secureauthx.server.organization.repository.OrganizationRepository;
import com.secureauthx.server.sessions.repository.SessionRepository;
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
class OrganizationControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private OrganizationMemberRepository organizationMemberRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    private String accessToken;
    private UUID userId;
    private UUID personalOrgId;

    @BeforeEach
    void setUp() throws Exception {
        userRoleRepository.deleteAll();
        organizationMemberRepository.deleteAll();
        organizationRepository.deleteAll();
        sessionRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();

        User user = userRepository.save(new User("org-user@example.com", passwordEncoder.encode("OrgPass!2026")));
        userId = user.getId();

        Organization personalOrg = organizationRepository.save(
                new Organization("My Org", "my-org", true));
        personalOrgId = personalOrg.getId();
        organizationMemberRepository.save(
                new OrganizationMember(personalOrg, user, OrganizationRole.OWNER));

        accessToken = loginAndGetToken("org-user@example.com", "OrgPass!2026");
    }

    @Test
    void listOrganizationsReturnsUserOrgs() throws Exception {
        mockMvc.perform(get("/api/v1/organizations")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("My Org"))
                .andExpect(jsonPath("$[0].role").value("OWNER"))
                .andExpect(jsonPath("$[0].isPersonal").value(true));
    }

    @Test
    void getCurrentOrganizationReturnsPersonalOrg() throws Exception {
        mockMvc.perform(get("/api/v1/organizations/current")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("My Org"))
                .andExpect(jsonPath("$.isPersonal").value(true));
    }

    @Test
    void createOrganizationReturns201() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/organizations")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Acme Corp"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Acme Corp"))
                .andExpect(jsonPath("$.role").value("OWNER"))
                .andReturn();

        String location = result.getResponse().getHeader("Location");
        assertThat(location).isNotNull();
    }

    @Test
    void updateOrganizationAsOwnerReturns200() throws Exception {
        mockMvc.perform(patch("/api/v1/organizations/{orgId}", personalOrgId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Updated Org"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Org"));
    }

    @Test
    void unauthenticatedRequestReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/organizations"))
                .andExpect(status().isForbidden());
    }

    @Test
    void memberCannotUpdateOrganization() throws Exception {
        User memberUser = userRepository.save(
                new User("member@example.com", passwordEncoder.encode("MemberPass!2026")));
        organizationMemberRepository.save(
                new OrganizationMember(
                        organizationRepository.findById(personalOrgId).orElseThrow(),
                        memberUser,
                        OrganizationRole.MEMBER));
        String memberToken = loginAndGetToken("member@example.com", "MemberPass!2026");

        mockMvc.perform(patch("/api/v1/organizations/{orgId}", personalOrgId)
                        .header("Authorization", "Bearer " + memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Hacked"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    @Sql("/seed-roles.sql")
    void registrationCreatesPersonalOrganization() throws Exception {
        String registerBody = """
                {"email": "newuser@example.com", "password": "NewUserPass!2026"}
                """;
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isCreated());

        String loginJson = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String newToken = objectMapper.readTree(loginJson).get("accessToken").asText();

        mockMvc.perform(get("/api/v1/organizations")
                        .header("Authorization", "Bearer " + newToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].isPersonal").value(true))
                .andExpect(jsonPath("$[0].role").value("OWNER"));
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
