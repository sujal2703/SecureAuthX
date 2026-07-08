package com.secureauthx.server.passkey.repository;

import com.secureauthx.server.passkey.entity.WebAuthnChallenge;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WebAuthnChallengeRepository extends JpaRepository<WebAuthnChallenge, UUID> {

    Optional<WebAuthnChallenge> findByChallenge(String challenge);
}
