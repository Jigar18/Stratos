package com.stratos.auth_service.controller;

import com.stratos.auth_service.dto.JWTTokenResponseDTO;
import com.stratos.auth_service.model.User;
import com.stratos.auth_service.service.GithubAuthService;
import com.stratos.auth_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("/callback")
    public ResponseEntity<JWTTokenResponseDTO> callback(@RequestParam("code") String code, @RequestParam("installation_id") String installationId) {
        User user = githubAuthService.processGithubLogin(code);

        JWTTokenResponseDTO jwtTokenResponseDTO = userService.provideGeneratedToken(user.getUsername());
        String refreshToken = userService.provideRefreshToken(user.getUsername());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, buildRefreshTokenCookie(refreshToken).toString())
                .body(jwtTokenResponseDTO);
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
