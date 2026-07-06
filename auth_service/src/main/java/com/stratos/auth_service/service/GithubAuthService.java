package com.stratos.auth_service.service;

import com.stratos.auth_service.dto.GithubUserDTO;
import com.stratos.auth_service.dto.GithubTokenResponseDTO;
import com.stratos.auth_service.model.GitHub;
import com.stratos.auth_service.model.User;
import com.stratos.auth_service.repository.GithubRepository;
import com.stratos.auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class GithubAuthService {
    private final RestClient restClient;
    private final GithubRepository githubRepository;
    private final UserRepository userRepository;

    @Value("${github.clientId}")
    private String clientId;
    @Value("${github.client-secret}")
    private String clientSecret;

    @Transactional
    public User processGithubLogin(String code) {
        GithubTokenResponseDTO token = fetchAccessToken(code);
        String accessToken = token.accessToken();
        GithubUserDTO githubUserDTO = fetchGithubUser(accessToken);
        return fetchUser(githubUserDTO, token);
    }

    private GithubTokenResponseDTO fetchAccessToken(String code) {
        Map<String, Object> body = Map.of(
                "client_id", clientId,
                "client_secret", clientSecret,
                "code", code
        );

        GithubTokenResponseDTO token;
        try {
            token = restClient.post()
                    .uri("https://github.com/login/oauth/access_token")
                    .header("Accept", "application/json")
                    .body(body)
                    .retrieve()
                    .body(GithubTokenResponseDTO.class);
        } catch (RestClientResponseException e) {
            throw githubAuthFailure("GitHub access-token exchange failed", e);
        }

        if (token == null || token.accessToken() == null || token.accessToken().isBlank()) {
            String message = token != null && token.errorDescription() != null
                    ? token.errorDescription()
                    : "GitHub did not return an access token";
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, message);
        }

        return token;
    }

    private GithubUserDTO fetchGithubUser(String accessToken) {
        try {
            return restClient.get()
                .uri("https://api.github.com/user")
                .header("Authorization", "Bearer " + accessToken)
                .header("Accept", "application/vnd.github+json")
                .header("X-GitHub-Api-Version", "2022-11-28")
                .header(HttpHeaders.USER_AGENT, "Stratos")
                .retrieve()
                .body(GithubUserDTO.class);
        } catch (RestClientResponseException e) {
            throw githubAuthFailure("GitHub user lookup failed", e);
        }
    }

    private ResponseStatusException githubAuthFailure(String message, RestClientResponseException e) {
        String responseBody = e.getResponseBodyAsString();
        String reason = responseBody.isBlank()
                ? message
                : message + ": " + responseBody;
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, reason, e);
    }

    private User fetchUser(GithubUserDTO githubUserDTO, GithubTokenResponseDTO githubTokenResponseDTO) {
        Optional<GitHub> existingUser = githubRepository.findByGitHubUserID(githubUserDTO.githubUserId());

        Instant now = Instant.now();
        Instant accessTokenExpiry = now.plusSeconds(githubTokenResponseDTO.accessTokenExpiresIn());
        Instant refreshTokenExpiry = now.plusSeconds(githubTokenResponseDTO.refreshTokenExpiresIn());

        if (existingUser.isPresent()) {
            GitHub gitHub = existingUser.get();
            gitHub.setAccessToken(githubTokenResponseDTO.accessToken());
            gitHub.setRefreshToken(githubTokenResponseDTO.refreshToken());
            gitHub.setAccessTokenExpiresAt(accessTokenExpiry);
            gitHub.setRefreshTokenExpiresAt(refreshTokenExpiry);
            githubRepository.saveAndFlush(gitHub);
            return gitHub.getUser();
        }

        User user = new User();
        user.setUsername(githubUserDTO.username());
        user.setEmail(githubUserDTO.email());
        user = userRepository.save(user);

        GitHub gitHub = new GitHub();
        gitHub.setUser(user);
        gitHub.setGitHubUserID(githubUserDTO.githubUserId());
        gitHub.setGitHubUserName(githubUserDTO.username());
        gitHub.setAccessToken(githubTokenResponseDTO.accessToken());
        gitHub.setRefreshToken(githubTokenResponseDTO.refreshToken());
        gitHub.setAccessTokenExpiresAt(accessTokenExpiry);
        gitHub.setRefreshTokenExpiresAt(refreshTokenExpiry);
        githubRepository.saveAndFlush(gitHub);

        return user;
    }
}
