package com.stratos.auth_service.controller;

import com.stratos.auth_service.dto.TokenResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

// https://handrail-breach-bride.ngrok-free.dev/api/github/callback?code=4472b070c5d16d1f7998&installation_id=144540237&setup_action=install

@RestController
@RequestMapping("api/github")
public class CallbackController {
    private final RestClient restClient;

    @Value("${github.clientId}")
    private String clientId;
    @Value("${github.cleint-secret}")
    private String clientSecret;

    public CallbackController(RestClient restClient) {
        this.restClient = restClient;
    }

    @GetMapping("/callback")
    public ResponseEntity<String> callback(@RequestParam("code") String code, @RequestParam("installation_id") String installationId) {
        if (code == null || code.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Code not found");
        }

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("code", code);
        body.add("installation_id", installationId);
        body.add("client_id", clientId);

        try {
            TokenResponseDTO tokenResponse = restClient.post()
                    .uri("https://github.com/login/oauth/access_token")
                    .header("Accept", "application/json")
                    .body(body)
                    .retrieve()
                    .body(TokenResponseDTO.class);

            String accessToken = tokenResponse.accessToken();

            return ResponseEntity.ok(accessToken);
        }
        catch (RestClientException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid client credentials");
        }
    }
}
