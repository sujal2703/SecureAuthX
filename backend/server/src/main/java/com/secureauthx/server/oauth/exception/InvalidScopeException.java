package com.secureauthx.server.oauth.exception;

public class InvalidScopeException extends OAuthException {
    public InvalidScopeException(String message) {
        super("invalid_scope", message);
    }
}
