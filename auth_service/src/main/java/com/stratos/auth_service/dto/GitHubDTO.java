package com.stratos.auth_service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GitHubDTO {
    private Long id;
    private Long githubUserId;
    private String githubUserName;
}
