package com.secureauthx.server.organization.exception;

public class OrganizationAccessDeniedException extends RuntimeException {

    public OrganizationAccessDeniedException() {
        super("You do not have access to this organization.");
    }
}
