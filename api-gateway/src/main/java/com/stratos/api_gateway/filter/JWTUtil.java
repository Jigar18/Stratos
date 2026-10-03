package com.stratos.api_gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JWTUtil {
    private final JwtParser parser;

    public JWTUtil(@Value("${jwt.secret-key}") String secretKey,
                   @Value("${jwt.issuer}") String issuer,
                   @Value("${jwt.audience}") String audience) {
        this.parser = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey)))
                .requireIssuer(issuer)
                .requireAudience(audience)
                .build();
    }

    public Claims validateToken(String token) {
        return parser.parseSignedClaims(token).getPayload();
    }
}
