package com.secureauthx.server.oauth.exception;

public class InvalidRedirectUriException extends OAuthException {
    public InvalidRedirectUriException(String message) {
        super("invalid_redirect_uri", message);
    }
}
