package com.stratos.auth_service.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.micrometer.observation.autoconfigure.ObservationProperties;
import org.springframework.http.HttpMethod;

// Routes that need no token. The gateway keeps the same list in its own PublicEndpoint.
@Getter
@RequiredArgsConstructor
public enum PublicEndpoint {
    REGISTER_USER(HttpMethod.POST, "/auth/register-user"),
    GENERATE_TOKEN(HttpMethod.POST, "/auth/generate-token"),
    REFRESH_TOKEN(HttpMethod.POST, "/auth/refresh-token"),
    REVOKE_REFRESH_TOKEN(HttpMethod.POST, "/auth/revoke-refresh-token"),
    GITHUB_LOGIN(HttpMethod.GET, "/api/github/login"),
    GITHUB_INSTALL(HttpMethod.GET, "/api/github/install"),
    GITHUB_CALLBACK(HttpMethod.GET, "/api/github/callback"),
    GITHUB_WEBHOOK(HttpMethod.POST, "/api/github/webhook");

    private final HttpMethod method;
    private final String path;
}
