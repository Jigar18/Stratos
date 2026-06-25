package com.stratos.auth_service.util;

import com.stratos.auth_service.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JWTUtil {
    private static final String SECRET_KEY = "7EB9818459D3E8757A7A8B514A5571C42959D7F7043C452EE2B6E9C797758CCB";

    private SecretKey getSigningKeys() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(User user){
        if (user.getId() == null) {
            throw new IllegalArgumentException("Cannot generate JWT before user has a database id");
        }

        return Jwts
                .builder()
                .claim("username", user.getUsername())
                .subject(String.valueOf(user.getId()))
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24))
                .signWith(getSigningKeys())
                .compact();
    }

    private Claims getClaims(String token){
        return Jwts
                .parser()
                .verifyWith((SecretKey) getSigningKeys())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Date getExpirationDate(String token){
        return getClaims(token).getExpiration();
    }
}
