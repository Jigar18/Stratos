package com.stratos.auth_service.util;

import com.stratos.auth_service.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JWTUtil {
    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.issuer}")
    private String issuer;

    @Value("${jwt.audience}")
    private String audience;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    private SecretKey getSigningKeys() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(User user){
        if (user.getId() == null) {
            throw new IllegalArgumentException("Cannot generate JWT before user has a database id");
        }

        return Jwts
                .builder()
                .claim("username", user.getUsername())
                .issuer(issuer)
                .audience().add(audience).and()
                .subject(String.valueOf(user.getId()))
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKeys())
                .compact();
    }

    public Claims validateToken(String token){
        return Jwts
                .parser()
                .verifyWith(getSigningKeys())
                .requireIssuer(issuer)
                .requireAudience(audience)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Date getExpirationDate(String token){
        return validateToken(token).getExpiration();
    }
}
