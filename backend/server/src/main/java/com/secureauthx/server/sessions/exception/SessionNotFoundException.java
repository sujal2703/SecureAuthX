package com.secureauthx.server.sessions.exception;

public class SessionNotFoundException extends RuntimeException {

    public SessionNotFoundException() {
        super("Session not found.");
    }
}
