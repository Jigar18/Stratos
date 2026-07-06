package com.stratos.auth_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TokenResponseDTO(@JsonProperty("access_token") String accessToken,
                               @JsonProperty("token_type") String tokenType,
                               String scope) { }
