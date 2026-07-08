package com.secureauthx.server.oauth.exception;

public class InvalidClientException extends OAuthException {
    public InvalidClientException(String message) {
        super("invalid_client", message);
    }
}
