package com.stratos.auth_service.dto;

import com.stratos.auth_service.model.GitHub;

public record InstallationStatusDTO(String installationId, String status) {
    public static InstallationStatusDTO from(GitHub account) {
        String status = account.getInstallationStatus() == null ? "NOT_CONNECTED" : account.getInstallationStatus().name();
        return new InstallationStatusDTO(account.getInstallationId(), status);
    }
}
