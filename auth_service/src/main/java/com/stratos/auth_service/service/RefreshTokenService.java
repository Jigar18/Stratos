package com.stratos.auth_service.service;

import com.stratos.auth_service.model.Token;
import com.stratos.auth_service.model.User;
import com.stratos.auth_service.repository.TokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class RefreshTokenService {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final TokenRepository tokenRepository;

    @Value("${refresh-token.expiration-ms:604800000}")
    private long refreshTokenExpirationMs;

    public RefreshTokenService(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @Transactional
    public String createOrReplaceRefreshToken(User user) {
        String rawRefreshToken = generateRefreshToken();
        String hashedRefreshToken = hashRefreshToken(rawRefreshToken);

        Token token = tokenRepository.findByUser(user).orElseGet(Token::new);
        token.setUser(user);
        token.setRefreshToken(hashedRefreshToken);
        token.setExpiresAt(LocalDateTime.now().plus(Duration.ofMillis(refreshTokenExpirationMs)));
        token.setRevoked(false);

        tokenRepository.save(token);
        return rawRefreshToken;
    }

    @Transactional
    public User validateRefreshToken(String rawRefreshToken) {
        Token token = tokenRepository.findByRefreshToken(hashRefreshToken(rawRefreshToken))
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        if (token.isRevoked() || token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        return token.getUser();
    }

    @Transactional
    public void revokeRefreshToken(String rawRefreshToken) {
        tokenRepository.findByRefreshToken(hashRefreshToken(rawRefreshToken))
                .ifPresent(token -> {
                    token.setRevoked(true);
                    tokenRepository.save(token);
                });
    }

    private String generateRefreshToken() {
        byte[] randomBytes = new byte[64];
        SECURE_RANDOM.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    private String hashRefreshToken(String refreshToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashedToken = digest.digest(refreshToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashedToken);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 hashing is not available", e);
        }
    }
}
