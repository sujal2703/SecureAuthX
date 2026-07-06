package com.secureauthx.server.auth.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {

    private static final int MINIMUM_LENGTH = 12;

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.length() < MINIMUM_LENGTH || password.length() > 128) {
            return false;
        }

        boolean hasUppercase = false;
        boolean hasLowercase = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;

        for (int index = 0; index < password.length(); index++) {
            char character = password.charAt(index);
            if (Character.isUpperCase(character)) {
                hasUppercase = true;
            } else if (Character.isLowerCase(character)) {
                hasLowercase = true;
            } else if (Character.isDigit(character)) {
                hasDigit = true;
            } else {
                hasSpecial = true;
            }
        }

        return hasUppercase && hasLowercase && hasDigit && hasSpecial;
    }
}
