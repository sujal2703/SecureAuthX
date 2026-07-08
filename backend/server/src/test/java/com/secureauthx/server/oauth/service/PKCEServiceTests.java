package com.secureauthx.server.oauth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.secureauthx.server.oauth.exception.InvalidGrantException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PKCEServiceTests {

    private PKCEService pkceService;

    @BeforeEach
    void setUp() {
        pkceService = new PKCEService();
    }

    @Test
    void generatesValidCodeVerifier() {
        String verifier = pkceService.generateCodeVerifier();
        assertThat(verifier).isNotBlank();
        assertThat(verifier.length()).isBetween(43, 128);
    }

    @Test
    void computesS256ChallengeCorrectly() {
        String verifier = "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk";
        String challenge = pkceService.computeS256Challenge(verifier);
        assertThat(challenge).isEqualTo("E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM");
    }

    @Test
    void verificationSucceedsWithValidChallenge() {
        String verifier = pkceService.generateCodeVerifier();
        String challenge = pkceService.computeS256Challenge(verifier);

        pkceService.verify(challenge, "S256", verifier);
    }

    @Test
    void verificationFailsWithWrongVerifier() {
        String challenge = pkceService.computeS256Challenge("correct-verifier-value-here-43chars-minimum-x");
        assertThatThrownBy(() -> pkceService.verify(challenge, "S256", "wrong-verifier-value-here-also-43-chars-long"))
                .isInstanceOf(InvalidGrantException.class)
                .hasMessageContaining("PKCE code verifier does not match");
    }

    @Test
    void verificationFailsWithNonS256Method() {
        assertThatThrownBy(() -> pkceService.verify("challenge", "plain", "verifier"))
                .isInstanceOf(InvalidGrantException.class)
                .hasMessageContaining("S256");
    }

    @Test
    void verificationFailsWithNullChallengeMethod() {
        assertThatThrownBy(() -> pkceService.verify("challenge", null, "verifier"))
                .isInstanceOf(InvalidGrantException.class)
                .hasMessageContaining("S256");
    }

    @Test
    void verificationFailsWithNullVerifier() {
        assertThatThrownBy(() -> pkceService.verify("challenge", "S256", null))
                .isInstanceOf(InvalidGrantException.class)
                .hasMessageContaining("PKCE code verifier is required");
    }

    @Test
    void verificationFailsWithEmptyVerifier() {
        assertThatThrownBy(() -> pkceService.verify("challenge", "S256", ""))
                .isInstanceOf(InvalidGrantException.class)
                .hasMessageContaining("PKCE code verifier is required");
    }

    @Test
    void verificationFailsWithShortVerifier() {
        assertThatThrownBy(() -> pkceService.verify("challenge", "S256", "short"))
                .isInstanceOf(InvalidGrantException.class)
                .hasMessageContaining("43 and 128 characters");
    }

    @Test
    void generateCodeChallengeCreatesValidChallenge() {
        String verifier = pkceService.generateCodeVerifier();
        String challenge = pkceService.generateCodeChallenge(verifier);
        assertThat(challenge).isNotBlank();
        assertThat(challenge).doesNotContain("=");
    }
}
