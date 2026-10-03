package com.stratos.auth_service.controller;

import com.stratos.auth_service.exception.GithubAuthorizationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

@Slf4j
@RestControllerAdvice
public class GithubExceptionHandler {
    // 409 rather than 401, so clients don't mistake a revoked GitHub token for an expired Stratos session.
    @ExceptionHandler({GithubAuthorizationException.class, HttpClientErrorException.Unauthorized.class})
    public ProblemDetail handleExpiredGithubAuthorization(Exception e) {
        log.info("GitHub authorization is no longer valid: {}", e.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "GitHub authorization expired or was revoked. Sign in with GitHub again.");
    }

    @ExceptionHandler(RestClientException.class)
    public ProblemDetail handleGithubFailure(RestClientException e) {
        log.warn("GitHub request failed", e);
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY, "GitHub request failed. Try again later.");
    }
}
