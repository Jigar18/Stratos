package com.stratos.api_gateway.filter;

import io.jsonwebtoken.IncorrectClaimException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JWTUtilTests {
    private static final String SECRET_KEY = "7EB9818459D3E8757A7A8B514A5571C42959D7F7043C452EE2B6E9C797758CCB";

    private final JWTUtil jwtUtil = new JWTUtil();

    @Test
    void rejectsTokenWithWrongAudience() {
        String token = Jwts.builder()
                .issuer("stratos-auth-service")
                .audience().add("some-other-audience").and()
                .subject("42")
                .claim("username", "jigar")
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000L * 60 * 60))
                .signWith(getSigningKey())
                .compact();

        assertThatThrownBy(() -> jwtUtil.validateToken(token))
                .isInstanceOf(IncorrectClaimException.class);
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
