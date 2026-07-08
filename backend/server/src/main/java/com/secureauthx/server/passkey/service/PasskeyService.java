package com.secureauthx.server.passkey.service;

import com.secureauthx.server.auth.entity.User;
import com.secureauthx.server.passkey.dto.PasskeyResponse;
import com.secureauthx.server.passkey.entity.Passkey;
import com.secureauthx.server.passkey.exception.PasskeyNotFoundException;
import com.secureauthx.server.passkey.repository.PasskeyRepository;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PasskeyService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PasskeyService.class);

    private final PasskeyRepository passkeyRepository;

    public PasskeyService(PasskeyRepository passkeyRepository) {
        this.passkeyRepository = passkeyRepository;
    }

    @Transactional
    public Passkey savePasskey(Passkey passkey) {
        Passkey saved = passkeyRepository.save(passkey);
        LOGGER.info("Passkey saved for user_id={} credential_id={}", saved.getUser().getId(),
                saved.getCredentialId());
        return saved;
    }

    public Passkey getPasskeyByCredentialId(String credentialId) {
        return passkeyRepository.findByCredentialId(credentialId)
                .orElseThrow(PasskeyNotFoundException::new);
    }

    public List<PasskeyResponse> getUserPasskeys(UUID userId) {
        return passkeyRepository.findByUserId(userId).stream()
                .map(PasskeyResponse::from)
                .toList();
    }

    @Transactional
    public void deletePasskey(UUID passkeyId, UUID userId) {
        Passkey passkey = passkeyRepository.findByIdAndUserId(passkeyId, userId)
                .orElseThrow(PasskeyNotFoundException::new);
        passkeyRepository.delete(passkey);
        LOGGER.info("Passkey deleted id={} user_id={}", passkeyId, userId);
    }

    public Passkey getPasskeyByIdAndUser(UUID passkeyId, UUID userId) {
        return passkeyRepository.findByIdAndUserId(passkeyId, userId)
                .orElseThrow(PasskeyNotFoundException::new);
    }
}
