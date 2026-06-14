package com.stratos.api_gateway.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

@Component
public class AuthFilter extends AbstractGatewayFilterFactory<AuthFilter.Config> {

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private Validator validator;

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            if (validator.predicate.test(exchange.getRequest())) {
                if (!exchange.getRequest().getHeaders().containsHeader(HttpHeaders.AUTHORIZATION)) {
                    throw new RuntimeException("Authorization Header is missing");
                }

                String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
                String token = null;

                if (authHeader != null && authHeader.contains("Bearer ")) {
                    token = authHeader.substring(7);
                }

                try {
                    jwtUtil.validateToken(token);
                }
                catch (Exception e) {
                    throw new RuntimeException("Token is invalid" + e.getMessage());
                }
            }

            return chain.filter(exchange);
        });
    }

    public static class Config {

    }
}
