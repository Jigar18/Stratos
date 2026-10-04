package com.stratos.auth_service.service;

import com.stratos.auth_service.client.GithubClient;
import com.stratos.auth_service.dto.GithubInstallationDTO;
import com.stratos.auth_service.dto.GithubTokenResponseDTO;
import com.stratos.auth_service.dto.GithubUserDTO;
import com.stratos.auth_service.dto.InstallationStatusDTO;
import com.stratos.auth_service.exception.InstallationNotAccessibleException;
import com.stratos.auth_service.exception.UsernameTakenException;
import com.stratos.auth_service.model.GitHub;
import com.stratos.auth_service.model.InstallationStatus;
import com.stratos.auth_service.model.User;
import com.stratos.auth_service.repository.GithubRepository;
import com.stratos.auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class GithubAuthService {
    private final GithubClient githubClient;
    private final GithubTokenService githubTokenService;
    private final GithubRepository githubRepository;
    private final UserRepository userRepository;

    @Transactional
    public GitHub processGithubLogin(String code, Long installationId) {
        GithubTokenResponseDTO token = githubClient.exchangeCode(code);
        GithubUserDTO profile = githubClient.fetchUser(token.accessToken());

        GitHub account = githubRepository.findByGitHubUserID(profile.githubUserId())
                .orElseGet(() -> createAccount(profile));
        account.setGitHubUserName(profile.username());
        githubTokenService.storeTokens(account, token);

        if (installationId != null) {
            GithubInstallationDTO installation = githubClient.findInstallation(token.accessToken(), installationId)
                    .orElseThrow(InstallationNotAccessibleException::new);
            setInstallation(account, installation);
        } else if (account.getInstallationId() != null) {
            // Re-check the stored installation in case an uninstall webhook was missed.
            githubClient.findInstallation(token.accessToken(), Long.parseLong(account.getInstallationId()))
                    .ifPresentOrElse(installation -> setInstallation(account, installation), () -> {
                        account.setInstallationId(null);
                        account.setInstallationStatus(null);
                    });
        }
        return githubRepository.save(account);
    }

    public InstallationStatusDTO getInstallation(long userId) {
        return githubRepository.findByUserId(userId)
                .map(InstallationStatusDTO::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Connect GitHub first"));
    }

    public InstallationStatusDTO linkInstallation(long userId, long installationId) {
        String accessToken = githubTokenService.getAccessToken(userId);
        GithubInstallationDTO installation = githubClient.findInstallation(accessToken, installationId)
                .orElseThrow(InstallationNotAccessibleException::new);

        String id = String.valueOf(installation.id());
        githubRepository.updateInstallation(userId, id, installation.status());
        return new InstallationStatusDTO(id, installation.status().name());
    }

    // Webhooks only update accounts that were already linked through a verified sign-in.
    public void handleInstallationEvent(String action, GithubInstallationDTO installation) {
        String installationId = String.valueOf(installation.id());
        switch (action) {
            case "deleted" -> githubRepository.disconnectInstallation(installationId);
            case "suspend" -> githubRepository.updateInstallationStatus(installationId, InstallationStatus.SUSPENDED);
            case "unsuspend" -> githubRepository.updateInstallationStatus(installationId, InstallationStatus.ACTIVE);
            default -> { }
        }
    }

    private GitHub createAccount(GithubUserDTO profile) {
        // Never attach a GitHub identity to an existing account just because the usernames match.
        if (userRepository.existsByUsername(profile.username())) {
            throw new UsernameTakenException(profile.username());
        }
        User user = new User();
        user.setUsername(profile.username());
        user.setEmail(profile.email());

        GitHub account = new GitHub();
        account.setUser(userRepository.save(user));
        account.setGitHubUserID(profile.githubUserId());
        return account;
    }

    private void setInstallation(GitHub account, GithubInstallationDTO installation) {
        account.setInstallationId(String.valueOf(installation.id()));
        account.setInstallationStatus(installation.status());
    }
}
