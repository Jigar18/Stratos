package com.stratos.auth_service.controller;

import com.stratos.auth_service.dto.JWTTokenResponseDTO;
import com.stratos.auth_service.dto.LoginRequestDTO;
import com.stratos.auth_service.dto.RegisterUserRequestDTO;
import com.stratos.auth_service.dto.UserDTO;
import com.stratos.auth_service.service.UserService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class UserController {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    @Value("${refresh-token.cookie-name:refreshToken}")
    private String refreshTokenCookieName;

    @Value("${refresh-token.expiration-ms:2592000000}")
    private long refreshTokenExpirationMs;

    @Value("${refresh-token.cookie-secure:false}")
    private boolean refreshTokenCookieSecure;

    public UserController(UserService userService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/register-user")
    public ResponseEntity<UserDTO> registerUser(@RequestBody RegisterUserRequestDTO request) {
        UserDTO userDTO = userService.saveUser(request);
        return new ResponseEntity<>(userDTO, HttpStatus.CREATED);
    }   

    @PostMapping("/generate-token")
    public ResponseEntity<JWTTokenResponseDTO> generateToken(@RequestBody LoginRequestDTO request) {
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

            if (authentication.isAuthenticated()) {
                JWTTokenResponseDTO jwtTokenResponseDTO = userService.provideGeneratedToken(request.getUsername());
                String refreshToken = userService.provideRefreshToken(request.getUsername());
                return ResponseEntity.ok()
                        .header(HttpHeaders.SET_COOKIE, buildRefreshTokenCookie(refreshToken).toString())
                        .body(jwtTokenResponseDTO);
            }
            else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        }
        catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<JWTTokenResponseDTO> refreshToken(@CookieValue(name = "${refresh-token.cookie-name:refreshToken}", required = false) String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            JWTTokenResponseDTO jwtTokenResponseDTO = userService.refreshJwtToken(refreshToken);
            String rotatedRefreshToken = userService.rotateRefreshToken(refreshToken);
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, buildRefreshTokenCookie(rotatedRefreshToken).toString())
                    .body(jwtTokenResponseDTO);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/revoke-refresh-token")
    public ResponseEntity<Void> revokeRefreshToken(@CookieValue(name = "${refresh-token.cookie-name:refreshToken}", required = false) String refreshToken) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            userService.revokeRefreshToken(refreshToken);
        }

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, clearRefreshTokenCookie().toString())
                .build();
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

    private ResponseCookie clearRefreshTokenCookie() {
        return ResponseCookie.from(refreshTokenCookieName, "")
                .httpOnly(true)
                .secure(refreshTokenCookieSecure)
                .sameSite("Strict")
                .path("/auth")
                .maxAge(0)
                .build();
    }
}
