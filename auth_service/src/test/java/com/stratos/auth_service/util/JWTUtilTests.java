package com.stratos.auth_service.util;

import com.stratos.auth_service.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;

import static org.assertj.core.api.Assertions.assertThat;

class JWTUtilTests {
    private static final String SECRET_KEY = "7EB9818459D3E8757A7A8B514A5571C42959D7F7043C452EE2B6E9C797758CCB";

    private final JWTUtil jwtUtil = new JWTUtil();

    @Test
    void generatedTokenContainsExpectedIssuerAndAudience() {
        User user = new User();
        user.setId(42L);
        user.setUsername("jigar");

        String token = jwtUtil.generateToken(user);

        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertThat(claims.getIssuer()).isEqualTo("stratos-auth-service");
        assertThat(claims.getAudience()).containsExactly("stratos-api-gateway");
        assertThat(claims.getSubject()).isEqualTo("42");
        assertThat(claims.get("username", String.class)).isEqualTo("jigar");
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
