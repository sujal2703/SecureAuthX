package com.secureauthx.server.oauth.exception;

public class InvalidGrantException extends OAuthException {
    public InvalidGrantException(String message) {
        super("invalid_grant", message);
    }
}
