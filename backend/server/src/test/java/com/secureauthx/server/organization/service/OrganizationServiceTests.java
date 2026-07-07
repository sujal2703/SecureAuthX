package com.secureauthx.server.organization.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.secureauthx.server.auth.entity.User;
import com.secureauthx.server.auth.repository.UserRepository;
import com.secureauthx.server.organization.dto.CreateOrganizationRequest;
import com.secureauthx.server.organization.dto.OrganizationResponse;
import com.secureauthx.server.organization.dto.UpdateOrganizationRequest;
import com.secureauthx.server.organization.entity.Organization;
import com.secureauthx.server.organization.entity.OrganizationMember;
import com.secureauthx.server.organization.entity.OrganizationRole;
import com.secureauthx.server.organization.exception.OrganizationAccessDeniedException;
import com.secureauthx.server.organization.repository.OrganizationMemberRepository;
import com.secureauthx.server.organization.repository.OrganizationRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceTests {

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private OrganizationMemberRepository organizationMemberRepository;

    @Mock
    private UserRepository userRepository;

    @Captor
    private ArgumentCaptor<Organization> organizationCaptor;

    @Captor
    private ArgumentCaptor<OrganizationMember> memberCaptor;

    private OrganizationService organizationService;

    private UUID userId;
    private User user;

    @BeforeEach
    void setUp() {
        organizationService = new OrganizationService(
                organizationRepository, organizationMemberRepository, userRepository
        );
        userId = UUID.randomUUID();
        user = new User("user@example.com", "$argon2id$hash");
        setField(user, "id", userId);
    }

    @Test
    void createsPersonalOrganizationWithOwnerRole() {
        when(organizationRepository.save(any(Organization.class))).thenAnswer(inv -> {
            Organization org = inv.getArgument(0);
            setField(org, "id", UUID.randomUUID());
            return org;
        });
        when(organizationMemberRepository.save(any(OrganizationMember.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Organization org = organizationService.createPersonalOrganization(user);

        assertThat(org).isNotNull();
        assertThat(org.getName()).contains("user@example.com");
        assertThat(org.isPersonal()).isTrue();

        verify(organizationMemberRepository).save(memberCaptor.capture());
        OrganizationMember member = memberCaptor.getValue();
        assertThat(member.getRole()).isEqualTo(OrganizationRole.OWNER);
    }

    @Test
    void createsOrganizationWithCreatorAsOwner() {
        String orgName = "Acme Corp";
        CreateOrganizationRequest request = new CreateOrganizationRequest(orgName);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(organizationRepository.save(any(Organization.class))).thenAnswer(inv -> {
            Organization org = inv.getArgument(0);
            setField(org, "id", UUID.randomUUID());
            return org;
        });
        when(organizationMemberRepository.save(any(OrganizationMember.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        OrganizationResponse response = organizationService.createOrganization(request, userId);

        assertThat(response.name()).isEqualTo(orgName);
        assertThat(response.role()).isEqualTo(OrganizationRole.OWNER);
    }

    @Test
    void listsUserOrganizations() {
        Organization org = new Organization("My Org", "my-org", false);
        setField(org, "id", UUID.randomUUID());
        OrganizationMember member = new OrganizationMember(org, user, OrganizationRole.ADMIN);

        when(organizationMemberRepository.findByUserIdWithOrganization(userId))
                .thenReturn(List.of(member));

        List<OrganizationResponse> orgs = organizationService.getUserOrganizations(userId);

        assertThat(orgs).hasSize(1);
        assertThat(orgs.getFirst().name()).isEqualTo("My Org");
        assertThat(orgs.getFirst().role()).isEqualTo(OrganizationRole.ADMIN);
    }

    @Test
    void returnsCurrentPersonalOrganization() {
        Organization personalOrg = new Organization("Personal", "personal-abc123", true);
        setField(personalOrg, "id", UUID.randomUUID());
        OrganizationMember member = new OrganizationMember(personalOrg, user, OrganizationRole.OWNER);

        Organization otherOrg = new Organization("Other", "other", false);
        setField(otherOrg, "id", UUID.randomUUID());
        OrganizationMember otherMember = new OrganizationMember(otherOrg, user, OrganizationRole.MEMBER);

        when(organizationMemberRepository.findByUserIdWithOrganization(userId))
                .thenReturn(List.of(otherMember, member));

        OrganizationResponse response = organizationService.getCurrentOrganization(userId);

        assertThat(response.isPersonal()).isTrue();
        assertThat(response.name()).isEqualTo("Personal");
    }

    @Test
    void updateOrganizationRequiresOwnerOrAdmin() {
        UUID orgId = UUID.randomUUID();
        Organization org = new Organization("Old Name", "old-name", false);
        setField(org, "id", orgId);
        OrganizationMember member = new OrganizationMember(org, user, OrganizationRole.MEMBER);

        when(organizationMemberRepository.findByOrganizationIdAndUserId(orgId, userId))
                .thenReturn(Optional.of(member));

        UpdateOrganizationRequest request = new UpdateOrganizationRequest("New Name");
        assertThatThrownBy(() -> organizationService.updateOrganization(orgId, request, userId))
                .isInstanceOf(OrganizationAccessDeniedException.class);
    }

    @Test
    void userCannotAccessOtherOrganization() {
        UUID orgId = UUID.randomUUID();
        when(organizationMemberRepository.findByOrganizationIdAndUserId(orgId, userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> organizationService.getOrganizationForUser(orgId, userId))
                .isInstanceOf(OrganizationAccessDeniedException.class);
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
