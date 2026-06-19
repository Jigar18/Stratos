package com.stratos.auth_service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDTO {
    private Long id;
    private String username;
    private String email;

    @Getter
    public static class LoginRequest {
        private String username;
        private String password;
    }
}
