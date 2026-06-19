package com.stratos.auth_service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JWTTokenResponseDTO {
    private String token;
    private String type;
    private String expiresAt;
}
