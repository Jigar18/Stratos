package com.stratos.auth_service.controller;

import com.stratos.auth_service.exception.GithubAuthorizationException;
import com.stratos.auth_service.exception.InstallationNotAccessibleException;
import com.stratos.auth_service.exception.UsernameTakenException;
import com.stratos.auth_service.model.GitHub;
import com.stratos.auth_service.model.InstallationStatus;
import com.stratos.auth_service.service.GithubAuthService;
import com.stratos.auth_service.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/github")
public class GithubAuthController {
    private static final String STATE_COOKIE_NAME = "github_oauth_state";
    private static final Duration STATE_COOKIE_MAX_AGE = Duration.ofMinutes(10);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final GithubAuthService githubAuthService;
    private final UserService userService;

    @Value("${refresh-token.cookie-name:refreshToken}")
    private String refreshTokenCookieName;
    @Value("${refresh-token.expiration-ms:2592000000}")
    private long refreshTokenExpirationMs;
    @Value("${refresh-token.cookie-secure:false}")
    private boolean secureCookies;
    @Value("${github.base-url}")
    private String baseURL;
    @Value("${github.clientId}")
    private String clientId;
    @Value("${github.callback-uri}")
    private String callbackUri;
    @Value("${github.installation-url}")
    private String installationUrl;

    @GetMapping("/login")
    public ResponseEntity<Void> login() {
        String state = generateState();
        URI authorizeUri = UriComponentsBuilder.fromUriString("https://github.com/login/oauth/authorize")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", callbackUri)
                .queryParam("state", state)
                .encode()
                .build()
                .toUri();
        return redirectWithState(authorizeUri, state);
    }

    @GetMapping("/install")
    public ResponseEntity<Void> install() {
        String state = generateState();
        URI installUri = UriComponentsBuilder.fromUriString(installationUrl)
                .queryParam("state", state)
                .build()
                .toUri();
        return redirectWithState(installUri, state);
    }

    // The browser lands here straight from github.com, so a failure redirects to the sign-in page
    // with an error code instead of returning an error response nobody would see rendered.
    @GetMapping("/callback")
    public ResponseEntity<Void> callback(@RequestParam(required = false) String code,
                                         @RequestParam(required = false) String state,
                                         @RequestParam(required = false) String error,
                                         @RequestParam(name = "installation_id", required = false) Long installationId,
                                         @CookieValue(name = STATE_COOKIE_NAME, required = false) String expectedState) {
        // The state must match the cookie set when this browser started the flow, which stops
        // an attacker from signing a victim into the attacker's account (login CSRF).
        if (state == null || !state.equals(expectedState)) {
            return redirectToSignIn("invalid_state");
        }
        // GitHub sends an error in place of the code when the user cancels on its authorization page.
        if (code == null) {
            return redirectToSignIn("access_denied".equals(error) ? "access_denied" : "sign_in_failed");
        }

        GitHub account;
        try {
            account = githubAuthService.processGithubLogin(code, installationId);
        } catch (UsernameTakenException e) {
            return redirectToSignIn("username_taken");
        } catch (InstallationNotAccessibleException e) {
            return redirectToSignIn("installation_not_accessible");
        } catch (GithubAuthorizationException | RestClientException e) {
            log.warn("GitHub sign-in failed", e);
            return redirectToSignIn("sign_in_failed");
        }
        String username = account.getUser().getUsername();
        String refreshToken = userService.provideRefreshToken(username);

        URI redirectUri = account.getInstallationStatus() == InstallationStatus.ACTIVE
                ? URI.create(baseURL + "/dashboard")
                : UriComponentsBuilder.fromUriString(baseURL + "/app-install")
                        .queryParam("login", username)
                        .encode()
                        .build()
                        .toUri();

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(redirectUri)
                .header(HttpHeaders.SET_COOKIE, buildRefreshTokenCookie(refreshToken).toString())
                .header(HttpHeaders.SET_COOKIE, buildStateCookie("", Duration.ZERO).toString())
                .build();
    }

    private ResponseEntity<Void> redirectToSignIn(String errorCode) {
        URI signInUri = UriComponentsBuilder.fromUriString(baseURL + "/")
                .queryParam("error", errorCode)
                .build()
                .toUri();
        return ResponseEntity.status(HttpStatus.FOUND).location(signInUri).build();
    }

    private String generateState() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private ResponseEntity<Void> redirectWithState(URI location, String state) {
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(location)
                .header(HttpHeaders.SET_COOKIE, buildStateCookie(state, STATE_COOKIE_MAX_AGE).toString())
                .build();
    }

    // Lax rather than Strict: the callback is a cross-site redirect from github.com,
    // and browsers drop Strict cookies on those.
    private ResponseCookie buildStateCookie(String state, Duration maxAge) {
        return ResponseCookie.from(STATE_COOKIE_NAME, state)
                .httpOnly(true)
                .secure(secureCookies)
                .sameSite("Lax")
                .path("/api/github")
                .maxAge(maxAge)
                .build();
    }

    private ResponseCookie buildRefreshTokenCookie(String refreshToken) {
        return ResponseCookie.from(refreshTokenCookieName, refreshToken)
                .httpOnly(true)
                .secure(secureCookies)
                .sameSite("Strict")
                .path("/auth")
                .maxAge(refreshTokenExpirationMs / 1000)
                .build();
    }
}
