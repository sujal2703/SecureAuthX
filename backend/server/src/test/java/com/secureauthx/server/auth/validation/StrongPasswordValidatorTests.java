package com.secureauthx.server.auth.validation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class StrongPasswordValidatorTests {

    private final StrongPasswordValidator validator = new StrongPasswordValidator();

    @Test
    void acceptsReasonablyStrongPassword() {
        assertThat(validator.isValid("S3cureExample!2026", null)).isTrue();
    }

    @Test
    void rejectsWeakPassword() {
        assertThat(validator.isValid("password", null)).isFalse();
        assertThat(validator.isValid("lowercaseonly1!", null)).isFalse();
        assertThat(validator.isValid("NOLOWERCASE1!", null)).isFalse();
        assertThat(validator.isValid("NoNumberHere!", null)).isFalse();
        assertThat(validator.isValid("NoSpecialHere1", null)).isFalse();
    }
}
