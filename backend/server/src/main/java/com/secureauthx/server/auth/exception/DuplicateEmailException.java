package com.secureauthx.server.auth.exception;

public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException(String email) {
        super("An account already exists for email: " + email);
    }
}
