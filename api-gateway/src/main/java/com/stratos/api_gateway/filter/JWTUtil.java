package com.stratos.api_gateway.filter;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class JWTUtil {
    private static final String SECRET_KEY =
            "sfjdgndjfsnifsjonflndsgjosnfjbvnjofHJAOFHUO2E40I3UWE0F8IDSHGUORHBNWUOFHVN0WIRHJB0WIJF40GH";

    private SecretKey getKey() {
        byte[] bytes = SECRET_KEY.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(bytes);
    }

    public Claims validateToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();              // return claims so the filter can use them
        } catch (ExpiredJwtException e) {
            throw new RuntimeException("Token expired", e);       // → 401, tell client to refresh
        } catch (UnsupportedJwtException | MalformedJwtException e) {
            throw new RuntimeException("Invalid token format", e); // → 401
        } catch (SignatureException e) {
            throw new RuntimeException("Invalid signature", e);    // → 401, possible tampering
        }
    }
}
