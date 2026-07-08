package com.secureauthx.server.developer.exception;

public class DeveloperProjectNotFoundException extends RuntimeException {
    public DeveloperProjectNotFoundException() {
        super("Developer project not found.");
    }
    public DeveloperProjectNotFoundException(String message) {
        super(message);
    }
}
