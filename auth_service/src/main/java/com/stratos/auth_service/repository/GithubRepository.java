package com.stratos.auth_service.repository;

import com.stratos.auth_service.model.GitHub;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GithubRepository extends JpaRepository<GitHub, Long> {
    Optional<GitHub> findByGitHubUserID(Long githubUserId);
}
