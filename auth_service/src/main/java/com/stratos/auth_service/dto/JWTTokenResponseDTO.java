package com.stratos.auth_service.dto;

import lombok.Data;

@Data
public class JWTTokenResponseDTO {
    private String token;
    private String type;
    private String expiresAt;
}
