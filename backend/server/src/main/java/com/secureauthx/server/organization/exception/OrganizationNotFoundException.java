package com.secureauthx.server.organization.exception;

public class OrganizationNotFoundException extends RuntimeException {

    public OrganizationNotFoundException() {
        super("Organization not found.");
    }

    public OrganizationNotFoundException(String message) {
        super(message);
    }
}
