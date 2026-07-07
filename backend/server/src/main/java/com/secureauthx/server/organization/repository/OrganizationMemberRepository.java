package com.secureauthx.server.organization.repository;

import com.secureauthx.server.organization.entity.OrganizationMember;
import com.secureauthx.server.organization.entity.OrganizationRole;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrganizationMemberRepository extends JpaRepository<OrganizationMember, UUID> {

    @Query("SELECT om FROM OrganizationMember om JOIN FETCH om.organization WHERE om.user.id = :userId")
    List<OrganizationMember> findByUserIdWithOrganization(@Param("userId") UUID userId);

    @Query("SELECT om FROM OrganizationMember om JOIN FETCH om.organization WHERE om.organization.id = :orgId AND om.user.id = :userId")
    Optional<OrganizationMember> findByOrganizationIdAndUserId(@Param("orgId") UUID orgId, @Param("userId") UUID userId);

    boolean existsByOrganizationIdAndUserId(UUID organizationId, UUID userId);

    boolean existsByOrganizationIdAndUserIdAndRole(UUID organizationId, UUID userId, OrganizationRole role);
}
