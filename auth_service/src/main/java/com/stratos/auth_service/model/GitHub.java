package com.stratos.auth_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Table(name = "github")
public class GitHub {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "github_user_id")
    private Long gitHubUserID;

    @Column(name = "github_user_name")
    private String gitHubUserName;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User userID;

    @PrePersist
    protected void onCreate() {
        LocalDateTime createdAt = LocalDateTime.now();
    }
}
