package com.secureauthx.server.passkey.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(name = "AuthenticateOptionsResponse")
public record AuthenticateOptionsResponse(
        String challenge,
        Long timeout,
        String rpId,
        List<AllowCredential> allowCredentials,
        String userVerification
) {
    public record AllowCredential(String type, String id, List<String> transports) {}

    public static AuthenticateOptionsResponse forAuthenticate(String challenge, String rpId,
                                                               List<AllowCredential> allowCredentials) {
        return new AuthenticateOptionsResponse(challenge, 60000L, rpId, allowCredentials, "required");
    }
}
