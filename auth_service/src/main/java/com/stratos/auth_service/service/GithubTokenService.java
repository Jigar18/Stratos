package com.stratos.auth_service.service;

import com.stratos.auth_service.client.GithubClient;
import com.stratos.auth_service.dto.GithubTokenResponseDTO;
import com.stratos.auth_service.exception.GithubAuthorizationException;
import com.stratos.auth_service.model.GitHub;
import com.stratos.auth_service.repository.GithubRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class GithubTokenService {
    private static final Duration EXPIRY_MARGIN = Duration.ofMinutes(1);

    private final GithubClient githubClient;
    private final GithubRepository githubRepository;

    // GitHub refresh tokens are single-use. The row lock stops two concurrent requests
    // from both refreshing, which would leave one of them holding a dead token.
    @Transactional
    public String getAccessToken(long userId) {
        GitHub account = githubRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Connect GitHub first"));

        if (account.getAccessToken() != null && !expiresWithin(account.getAccessTokenExpiresAt(), EXPIRY_MARGIN)) {
            return account.getAccessToken();
        }
        if (account.getRefreshToken() == null || expiresWithin(account.getRefreshTokenExpiresAt(), Duration.ZERO)) {
            throw new GithubAuthorizationException("GitHub refresh token has expired");
        }

        storeTokens(account, githubClient.refreshToken(account.getRefreshToken()));
        return account.getAccessToken();
    }

    public void storeTokens(GitHub account, GithubTokenResponseDTO token) {
        account.setAccessToken(token.accessToken());
        account.setAccessTokenExpiresAt(expiryFrom(token.accessTokenExpiresIn()));
        account.setRefreshToken(token.refreshToken());
        account.setRefreshTokenExpiresAt(expiryFrom(token.refreshTokenExpiresIn()));
    }

    // A null expiry means GitHub issued a non-expiring token.
    private boolean expiresWithin(Instant expiresAt, Duration margin) {
        return expiresAt != null && expiresAt.isBefore(Instant.now().plus(margin));
    }

    private Instant expiryFrom(Long expiresInSeconds) {
        return expiresInSeconds == null ? null : Instant.now().plusSeconds(expiresInSeconds);
    }
}
