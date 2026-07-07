package com.secureauthx.server.organization.service;

import com.secureauthx.server.auth.entity.User;
import com.secureauthx.server.auth.repository.UserRepository;
import com.secureauthx.server.organization.dto.CreateOrganizationRequest;
import com.secureauthx.server.organization.dto.OrganizationResponse;
import com.secureauthx.server.organization.dto.UpdateOrganizationRequest;
import com.secureauthx.server.organization.entity.Organization;
import com.secureauthx.server.organization.entity.OrganizationMember;
import com.secureauthx.server.organization.entity.OrganizationRole;
import com.secureauthx.server.organization.exception.OrganizationAccessDeniedException;
import com.secureauthx.server.organization.exception.OrganizationNotFoundException;
import com.secureauthx.server.organization.repository.OrganizationMemberRepository;
import com.secureauthx.server.organization.repository.OrganizationRepository;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrganizationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrganizationService.class);

    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final UserRepository userRepository;

    public OrganizationService(
            OrganizationRepository organizationRepository,
            OrganizationMemberRepository organizationMemberRepository,
            UserRepository userRepository
    ) {
        this.organizationRepository = organizationRepository;
        this.organizationMemberRepository = organizationMemberRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Organization createPersonalOrganization(User user) {
        String slug = "personal-" + user.getId().toString().substring(0, 8);
        Organization org = new Organization(
                user.getEmail() + "'s Organization",
                slug,
                true
        );
        Organization saved = organizationRepository.save(org);
        organizationMemberRepository.save(new OrganizationMember(saved, user, OrganizationRole.OWNER));
        LOGGER.info("Personal organization created org_id={} for user_id={}", saved.getId(), user.getId());
        return saved;
    }

    @Transactional
    public OrganizationResponse createOrganization(CreateOrganizationRequest request, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found: " + userId));

        String slug = generateSlug(request.name());
        if (organizationRepository.existsBySlug(slug)) {
            slug = slug + "-" + UUID.randomUUID().toString().substring(0, 6);
        }
        Organization org = new Organization(request.name(), slug, false);
        Organization saved = organizationRepository.save(org);
        organizationMemberRepository.save(new OrganizationMember(saved, user, OrganizationRole.OWNER));
        LOGGER.info("Organization created org_id={} by user_id={}", saved.getId(), userId);
        return OrganizationResponse.from(saved, OrganizationRole.OWNER);
    }

    @Transactional(readOnly = true)
    public List<OrganizationResponse> getUserOrganizations(UUID userId) {
        return organizationMemberRepository.findByUserIdWithOrganization(userId)
                .stream()
                .map(om -> OrganizationResponse.from(om.getOrganization(), om.getRole()))
                .toList();
    }

    @Transactional(readOnly = true)
    public OrganizationResponse getCurrentOrganization(UUID userId) {
        List<OrganizationMember> memberships = organizationMemberRepository.findByUserIdWithOrganization(userId);
        return memberships.stream()
                .filter(om -> om.getOrganization().isPersonal())
                .findFirst()
                .map(om -> OrganizationResponse.from(om.getOrganization(), om.getRole()))
                .orElseGet(() -> memberships.stream()
                        .findFirst()
                        .map(om -> OrganizationResponse.from(om.getOrganization(), om.getRole()))
                        .orElseThrow(OrganizationNotFoundException::new));
    }

    @Transactional(readOnly = true)
    public Organization getOrganizationForUser(UUID organizationId, UUID userId) {
        OrganizationMember member = organizationMemberRepository
                .findByOrganizationIdAndUserId(organizationId, userId)
                .orElseThrow(OrganizationAccessDeniedException::new);
        return member.getOrganization();
    }

    @Transactional
    public OrganizationResponse updateOrganization(UUID organizationId, UpdateOrganizationRequest request, UUID userId) {
        OrganizationMember member = organizationMemberRepository
                .findByOrganizationIdAndUserId(organizationId, userId)
                .orElseThrow(OrganizationAccessDeniedException::new);

        if (member.getRole() != OrganizationRole.OWNER && member.getRole() != OrganizationRole.ADMIN) {
            throw new OrganizationAccessDeniedException();
        }

        Organization org = member.getOrganization();
        org.setName(request.name());
        String newSlug = generateSlug(request.name());
        if (!organizationRepository.existsBySlug(newSlug) || newSlug.equals(org.getSlug())) {
            org.setSlug(newSlug);
        }
        Organization saved = organizationRepository.save(org);
        LOGGER.info("Organization updated org_id={} by user_id={}", saved.getId(), userId);
        return OrganizationResponse.from(saved, member.getRole());
    }

    private String generateSlug(String name) {
        return name.trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }
}
