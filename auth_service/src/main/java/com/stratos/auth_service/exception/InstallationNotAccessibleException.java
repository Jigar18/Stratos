package com.stratos.auth_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class InstallationNotAccessibleException extends ResponseStatusException {
    public InstallationNotAccessibleException() {
        super(HttpStatus.FORBIDDEN, "This GitHub installation is not accessible to you");
    }
}
