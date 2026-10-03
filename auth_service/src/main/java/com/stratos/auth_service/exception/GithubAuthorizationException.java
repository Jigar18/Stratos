package com.stratos.auth_service.exception;

public class GithubAuthorizationException extends RuntimeException {
    public GithubAuthorizationException(String message) {
        super(message);
    }
}
