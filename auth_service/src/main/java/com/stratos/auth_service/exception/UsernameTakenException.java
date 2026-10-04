package com.stratos.auth_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class UsernameTakenException extends ResponseStatusException {
    public UsernameTakenException(String username) {
        super(HttpStatus.CONFLICT, "The username " + username + " is already taken by another Stratos account");
    }
}
