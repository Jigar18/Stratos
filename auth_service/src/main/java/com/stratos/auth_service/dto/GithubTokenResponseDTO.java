package com.stratos.auth_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GithubTokenResponseDTO(@JsonProperty("access_token") String accessToken,
                                     @JsonProperty("expires_in") long accessTokenExpiresIn,
                                     @JsonProperty("refresh_token") String refreshToken,
                                     @JsonProperty("refresh_token_expires_in") long refreshTokenExpiresIn,
                                     @JsonProperty("token_type") String tokenType,
                                     String scope,
                                     String error,
                                     @JsonProperty("error_description") String errorDescription) { }
