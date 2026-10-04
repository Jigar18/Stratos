package com.stratos.api_gateway.filter;

import org.springframework.http.HttpMethod;

import java.util.Arrays;

// Backend routes that need no token. auth_service keeps the same list in its own PublicEndpoint.
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

    PublicEndpoint(HttpMethod method, String path) {
        this.method = method;
        this.path = path;
    }

    public static boolean matches(HttpMethod method, String path) {
        return Arrays.stream(values())
                .anyMatch(endpoint -> endpoint.method.equals(method) && endpoint.path.equals(path));
    }
}
