package com.stratos.auth_service.controller;

import com.stratos.auth_service.dto.GithubInstallationEventDTO;
import com.stratos.auth_service.service.GithubAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.HexFormat;

@RestController
@RequiredArgsConstructor
public class GithubWebhookController {
    private final GithubAuthService githubAuthService;
    private final ObjectMapper objectMapper;

    @Value("${github.webhook-secret}")
    private String webhookSecret;

    // The body is read as raw bytes because the signature is computed over the exact payload.
    @PostMapping("/api/github/webhook")
    public ResponseEntity<Void> handleWebhook(@RequestHeader("X-GitHub-Event") String event,
                                              @RequestHeader("X-Hub-Signature-256") String signature,
                                              @RequestBody byte[] payload) {
        if (!isSignatureValid(payload, signature)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (event.equals("installation")) {
            GithubInstallationEventDTO installationEvent = objectMapper.readValue(payload, GithubInstallationEventDTO.class);
            githubAuthService.handleInstallationEvent(installationEvent.action(), installationEvent.installation());
        }
        return ResponseEntity.noContent().build();
    }

    private boolean isSignatureValid(byte[] payload, String signature) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            String expected = "sha256=" + HexFormat.of().formatHex(mac.doFinal(payload));
            // Constant-time comparison so the signature can't be guessed byte by byte from response timing.
            return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), signature.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("HmacSHA256 is not available", e);
        }
    }
}
