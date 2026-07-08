package com.secureauthx.server.developer.exception;

public class DeveloperAccessDeniedException extends RuntimeException {
    public DeveloperAccessDeniedException() {
        super("Access denied to this developer resource.");
    }
    public DeveloperAccessDeniedException(String message) {
        super(message);
    }
}
