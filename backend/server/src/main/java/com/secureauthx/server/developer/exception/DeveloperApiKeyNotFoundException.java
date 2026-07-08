package com.secureauthx.server.developer.exception;

public class DeveloperApiKeyNotFoundException extends RuntimeException {
    public DeveloperApiKeyNotFoundException() {
        super("API key not found.");
    }
    public DeveloperApiKeyNotFoundException(String message) {
        super(message);
    }
}
