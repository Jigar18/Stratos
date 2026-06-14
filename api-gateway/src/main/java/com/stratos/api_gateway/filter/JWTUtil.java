package com.stratos.api_gateway.filter;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Base64;

@Component
public class JWTUtil {
    public static final String SECRET_KEY = "sfjdgndjfsnifsjonflndsgjosnfjbvnjofHJAOFHUO2E40I3UWE0F8IDSHGUORHBNWUOFHVN0WIRHJB0WIJF40GH";
    private Key  getKey() {
        byte[] bytes = Base64.getDecoder().decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(bytes);
    }

    public void validateToken(String token) {
        Jwts.parser()
                .verifyWith((SecretKey) getKey())
                .build()
                .parseSignedClaims(token);
    }
}
