package com.stratos.auth_service.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
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
