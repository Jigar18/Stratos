package com.stratos.auth_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GithubUserDTO(@JsonProperty("id") Long githubUserId,
                            @JsonProperty("login") String username,
                            @JsonProperty("email") String email) {
}
