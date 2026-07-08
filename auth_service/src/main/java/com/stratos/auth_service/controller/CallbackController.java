package com.stratos.auth_service.controller;

import com.stratos.auth_service.model.User;
import com.stratos.auth_service.service.GithubAuthService;
import com.stratos.auth_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

// https://handrail-breach-bride.ngrok-free.dev/api/github/callback?code=e56c9a961da868547fc6&installation_id=145046293&setup_action=install

@RestController
@RequiredArgsConstructor
@RequestMapping("api/github")
public class CallbackController {
    private final GithubAuthService githubAuthService;
    private final UserService userService;

    @Value("${refresh-token.cookie-name:refreshToken}")
    private String refreshTokenCookieName;
    @Value("${refresh-token.expiration-ms:2592000000}")
    private long refreshTokenExpirationMs;
    @Value("${refresh-token.cookie-secure:false}")
    private boolean refreshTokenCookieSecure;
    @Value("${github.base-url}")
    private String baseURL;

    @GetMapping("/callback")
    public ResponseEntity<Void> callback(@RequestParam("code") String code, @RequestParam(value = "installation_id", required = false) String installationId) {
        User user = githubAuthService.processGithubLogin(code, installationId);

        String refreshToken = userService.provideRefreshToken(user.getUsername());

        String redirectURL = hasText(user.getInstallationId())
                ? baseURL + "/dashboard"
                : baseURL + "/app-install?login=" + URLEncoder.encode(user.getUsername(), StandardCharsets.UTF_8);

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(redirectURL))
                .header(HttpHeaders.SET_COOKIE, buildRefreshTokenCookie(refreshToken).toString())
                .build();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private ResponseCookie buildRefreshTokenCookie(String refreshToken) {
        return ResponseCookie.from(refreshTokenCookieName, refreshToken)
                .httpOnly(true)
                .secure(refreshTokenCookieSecure)
                .sameSite("Strict")
                .path("/auth")
                .maxAge(refreshTokenExpirationMs / 1000)
                .build();
    }
}
