package com.stratos.auth_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.stratos.auth_service.model.InstallationStatus;

import java.time.Instant;

public record GithubInstallationDTO(long id,
                                    @JsonProperty("suspended_at") Instant suspendedAt) {
    public InstallationStatus status() {
        return suspendedAt == null ? InstallationStatus.ACTIVE : InstallationStatus.SUSPENDED;
    }
}
