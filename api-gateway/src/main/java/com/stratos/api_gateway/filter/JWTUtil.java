package com.stratos.api_gateway.filter;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

@Component
public class JWTUtil {
    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.issuer}")
    private String expectedIssuer;

    @Value("${jwt.audience}")
    private String expectedAudience;

    private SecretKey getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Claims validateToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getKey())
                    .requireIssuer(expectedIssuer)
                    .requireAudience(expectedAudience)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw e;
        } catch (UnsupportedJwtException | MalformedJwtException e) {
            throw e;
        } catch (SignatureException e) {
            throw e;
        }
    }
}
