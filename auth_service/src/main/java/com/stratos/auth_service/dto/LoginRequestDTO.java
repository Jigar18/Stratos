package com.stratos.auth_service.dto;

import lombok.Getter;

@Getter
public class LoginRequestDTO {
    private String username;
    private String password;
}
