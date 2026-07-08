package com.secureauthx.server.oauth.exception;

public class UnauthorizedClientException extends OAuthException {
    public UnauthorizedClientException(String message) {
        super("unauthorized_client", message);
    }
}
