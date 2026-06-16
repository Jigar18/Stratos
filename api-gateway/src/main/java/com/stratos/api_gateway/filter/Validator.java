package com.stratos.api_gateway.filter;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.util.List;
import java.util.function.Predicate;

@Component
public class Validator {
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public static final List<String> endponints = List.of(
            "/auth/register-user",
            "/auth/generate-token",
            "/auth/validate-token/{token}"
    );

    public Predicate<ServerHttpRequest> predicate = serverHttpRequest -> {
        String requestPath = serverHttpRequest.getURI().getPath();

        return endponints.stream().
                noneMatch(uri -> pathMatcher.match(uri, requestPath));
    };
}
