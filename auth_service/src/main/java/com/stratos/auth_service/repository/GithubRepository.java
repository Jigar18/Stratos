package com.stratos.auth_service.repository;

import com.stratos.auth_service.model.GitHub;
import com.stratos.auth_service.model.InstallationStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface GithubRepository extends JpaRepository<GitHub, Long> {
    Optional<GitHub> findByGitHubUserID(Long githubUserId);

    Optional<GitHub> findByUserId(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select g from GitHub g where g.user.id = :userId")
    Optional<GitHub> findByUserIdForUpdate(Long userId);

    @Transactional
    @Modifying
    @Query("update GitHub g set g.installationId = :installationId, g.installationStatus = :status where g.user.id = :userId")
    void updateInstallation(Long userId, String installationId, InstallationStatus status);

    @Transactional
    @Modifying
    @Query("update GitHub g set g.installationStatus = :status where g.installationId = :installationId")
    void updateInstallationStatus(String installationId, InstallationStatus status);

    @Transactional
    @Modifying
    @Query("update GitHub g set g.installationId = null, g.installationStatus = null where g.installationId = :installationId")
    void disconnectInstallation(String installationId);
}
