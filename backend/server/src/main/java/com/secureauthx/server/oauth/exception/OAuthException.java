package com.secureauthx.server.oauth.exception;

public class OAuthException extends RuntimeException {
    private final String errorCode;

    public OAuthException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() { return errorCode; }
}
