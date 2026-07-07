package com.secureauthx.server.organization.dto;

import com.secureauthx.server.organization.entity.Organization;
import com.secureauthx.server.organization.entity.OrganizationRole;
import java.time.OffsetDateTime;
import java.util.UUID;

public record OrganizationResponse(
        UUID id,
        String name,
        String slug,
        boolean isPersonal,
        OrganizationRole role,
        OffsetDateTime createdAt
) {
    public static OrganizationResponse from(Organization organization, OrganizationRole role) {
        return new OrganizationResponse(
                organization.getId(),
                organization.getName(),
                organization.getSlug(),
                organization.isPersonal(),
                role,
                organization.getCreatedAt()
        );
    }
}
