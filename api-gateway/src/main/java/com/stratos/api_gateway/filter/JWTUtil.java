package com.stratos.api_gateway.filter;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class JWTUtil {
    private static final String SECRET_KEY = "44EFF29E658A0CBB0B7E849586DB2171144B1C6E210FA7A9D9BAE01D30174D43";

    private SecretKey getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Claims validateToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new RuntimeException("Token expired", e);
        } catch (UnsupportedJwtException | MalformedJwtException e) {
            throw new RuntimeException("Invalid token format", e);
        } catch (SignatureException e) {
            throw new RuntimeException("Invalid signature", e);
        }
    }
}
