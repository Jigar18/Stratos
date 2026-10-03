package com.stratos.auth_service.controller;

import com.stratos.auth_service.dto.InstallationStatusDTO;
import com.stratos.auth_service.service.GithubAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/github/installation")
public class GithubInstallationController {
    private final GithubAuthService githubAuthService;

    @GetMapping
    public InstallationStatusDTO getInstallation(@AuthenticationPrincipal Long userId) {
        return githubAuthService.getInstallation(userId);
    }

    @PutMapping
    public InstallationStatusDTO linkInstallation(@AuthenticationPrincipal Long userId,
                                                  @RequestParam("installation_id") long installationId) {
        return githubAuthService.linkInstallation(userId, installationId);
    }
}
