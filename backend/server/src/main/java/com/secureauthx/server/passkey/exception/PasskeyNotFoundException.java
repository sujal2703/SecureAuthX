package com.secureauthx.server.passkey.exception;

public class PasskeyNotFoundException extends RuntimeException {

    public PasskeyNotFoundException() {
        super("Passkey not found.");
    }
}
