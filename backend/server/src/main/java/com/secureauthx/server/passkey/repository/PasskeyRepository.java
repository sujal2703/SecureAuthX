package com.secureauthx.server.passkey.repository;

import com.secureauthx.server.passkey.entity.Passkey;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasskeyRepository extends JpaRepository<Passkey, UUID> {

    List<Passkey> findByUserId(UUID userId);

    Optional<Passkey> findByCredentialId(String credentialId);

    Optional<Passkey> findByIdAndUserId(UUID id, UUID userId);

    void deleteByIdAndUserId(UUID id, UUID userId);
}
