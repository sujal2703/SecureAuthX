package com.secureauthx.server.oauth.repository;

import com.secureauthx.server.oauth.entity.AuthorizationCode;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorizationCodeRepository extends JpaRepository<AuthorizationCode, UUID> {
    Optional<AuthorizationCode> findByCode(String code);
}
