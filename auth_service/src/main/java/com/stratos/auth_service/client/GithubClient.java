package com.stratos.auth_service.client;

import com.stratos.auth_service.dto.GithubInstallationDTO;
import com.stratos.auth_service.dto.GithubInstallationListDTO;
import com.stratos.auth_service.dto.GithubTokenResponseDTO;
import com.stratos.auth_service.dto.GithubUserDTO;
import com.stratos.auth_service.exception.GithubAuthorizationException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class GithubClient {
    private static final String TOKEN_URL = "https://github.com/login/oauth/access_token";
    private static final String API_URL = "https://api.github.com";

    private final RestClient restClient;

    @Value("${github.clientId}")
    private String clientId;
    @Value("${github.client-secret}")
    private String clientSecret;

    public GithubTokenResponseDTO exchangeCode(String code) {
        return requestToken(Map.of(
                "client_id", clientId,
                "client_secret", clientSecret,
                "code", code));
    }

    public GithubTokenResponseDTO refreshToken(String refreshToken) {
        return requestToken(Map.of(
                "client_id", clientId,
                "client_secret", clientSecret,
                "grant_type", "refresh_token",
                "refresh_token", refreshToken));
    }

    public GithubUserDTO fetchUser(String accessToken) {
        return get("/user", accessToken).body(GithubUserDTO.class);
    }

    // A GitHub App user token only lists installations of this app that the user can access,
    // so a match here proves the user is allowed to link the installation.
    public Optional<GithubInstallationDTO> findInstallation(String accessToken, long installationId) {
        GithubInstallationListDTO response = get("/user/installations?per_page=100", accessToken)
                .body(GithubInstallationListDTO.class);
        return response.installations().stream()
                .filter(installation -> installation.id() == installationId)
                .findFirst();
    }

    private GithubTokenResponseDTO requestToken(Map<String, String> params) {
        GithubTokenResponseDTO token = restClient.post()
                .uri(TOKEN_URL)
                .accept(MediaType.APPLICATION_JSON)
                .body(params)
                .retrieve()
                .body(GithubTokenResponseDTO.class);

        // GitHub reports OAuth errors such as an expired code with a 200 status and an error field.
        if (token == null || token.accessToken() == null) {
            String reason = token == null ? "empty token response" : token.error();
            throw new GithubAuthorizationException("GitHub token request failed: " + reason);
        }
        return token;
    }

    private RestClient.ResponseSpec get(String path, String accessToken) {
        return restClient.get()
                .uri(API_URL + path)
                .headers(headers -> {
                    headers.setBearerAuth(accessToken);
                    headers.set(HttpHeaders.ACCEPT, "application/vnd.github+json");
                    headers.set(HttpHeaders.USER_AGENT, "Stratos");
                    headers.set("X-GitHub-Api-Version", "2022-11-28");
                })
                .retrieve();
    }
}
