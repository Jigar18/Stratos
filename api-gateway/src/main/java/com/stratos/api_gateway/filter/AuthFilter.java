package com.stratos.api_gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Set;

@Component
public class AuthFilter implements GlobalFilter, Ordered {
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USERNAME_HEADER = "X-Username";

    private static final Set<String> PUBLIC_ENDPOINTS = Set.of(
            "POST /auth/register-user",
            "POST /auth/generate-token",
            "POST /auth/refresh-token",
            "POST /auth/revoke-refresh-token",
            "GET /api/github/login",
            "GET /api/github/install",
            "GET /api/github/callback",
            "POST /api/github/webhook"
    );

    private final JWTUtil jwtUtil;

    public AuthFilter(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Downstream services trust these headers, so only the gateway may set them.
        ServerHttpRequest request = exchange.getRequest().mutate()
                .headers(headers -> {
                    headers.remove(USER_ID_HEADER);
                    headers.remove(USERNAME_HEADER);
                })
                .build();

        if (!requiresAuthentication(request)) {
            return chain.filter(exchange.mutate().request(request).build());
        }

        String authorization = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return unauthorized(exchange, "Missing bearer token");
        }

        try {
            Claims claims = jwtUtil.validateToken(authorization.substring(7));
            ServerHttpRequest authenticatedRequest = request.mutate()
                    .headers(headers -> {
                        headers.set(USER_ID_HEADER, claims.getSubject());
                        headers.set(USERNAME_HEADER, claims.get("username", String.class));
                    })
                    .build();
            return chain.filter(exchange.mutate().request(authenticatedRequest).build());
        } catch (ExpiredJwtException e) {
            return unauthorized(exchange, "Token has expired");
        } catch (JwtException | IllegalArgumentException e) {
            return unauthorized(exchange, "Invalid token");
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    // Frontend pages stay public; only backend routes need a token.
    private boolean requiresAuthentication(ServerHttpRequest request) {
        String path = request.getPath().value();
        boolean isBackendPath = path.startsWith("/auth/") || path.startsWith("/api/");
        return isBackendPath && !PUBLIC_ENDPOINTS.contains(request.getMethod().name() + " " + path);
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        response.getHeaders().set(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
        byte[] body = ("{\"error\":\"" + message + "\"}").getBytes(StandardCharsets.UTF_8);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body)));
    }
}
