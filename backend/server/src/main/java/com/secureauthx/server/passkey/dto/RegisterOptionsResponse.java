package com.secureauthx.server.passkey.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(name = "RegisterOptionsResponse")
public record RegisterOptionsResponse(
        String challenge,
        Rp rp,
        User user,
        List<PubKeyCredParam> pubKeyCredParams,
        AuthenticatorSelection authenticatorSelection,
        List<String> hints,
        Attestation attestation
) {
    public record Rp(String name, String id) {}
    public record User(String id, String name, String displayName) {}
    public record PubKeyCredParam(String type, int alg) {}
    public record AuthenticatorSelection(
            String residentKey, String userVerification, boolean requireResidentKey
    ) {}
    public record Attestation(String fmt, int alg) {}

    public static RegisterOptionsResponse forRegister(String challenge, String rpId, String rpName,
                                                       String userId, String userName) {
        return new RegisterOptionsResponse(
                challenge,
                new Rp(rpName, rpId),
                new User(userId, userName, userName),
                List.of(
                        new PubKeyCredParam("public-key", -7),
                        new PubKeyCredParam("public-key", -257)
                ),
                new AuthenticatorSelection("required", "required", true),
                List.of("security-key", "platform"),
                new Attestation("none", -7)
        );
    }
}
