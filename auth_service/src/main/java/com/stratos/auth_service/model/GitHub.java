package com.stratos.auth_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Table(name = "github", schema = "auth_service")
public class GitHub {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "github_user_id")
    private Long gitHubUserID;

    @Column(name = "github_user_name")
    private String gitHubUserName;

    @Column(name = "created_at")
    private Instant createdAt;

    private String accessToken;
    private Instant accessTokenExpiresAt;

    @Column(name = "refresh_token", unique = true)
    private String refreshToken;

    private Instant refreshTokenExpiresAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}
