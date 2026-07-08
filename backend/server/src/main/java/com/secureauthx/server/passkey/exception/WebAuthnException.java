package com.secureauthx.server.passkey.exception;

public class WebAuthnException extends RuntimeException {

    public WebAuthnException(String message) {
        super(message);
    }
}
