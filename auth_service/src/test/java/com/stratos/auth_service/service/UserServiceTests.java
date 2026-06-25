package com.stratos.auth_service.service;

import com.stratos.auth_service.dto.RegisterUserRequestDTO;
import com.stratos.auth_service.model.User;
import com.stratos.auth_service.repository.UserRepository;
import com.stratos.auth_service.util.JWTUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JWTUtil jwtUtil;

    @InjectMocks
    private UserService userService;

    @Test
    void registrationCreatesANewUserFromAllowedFields() {
        RegisterUserRequestDTO request = new RegisterUserRequestDTO(
                "jigar",
                "plain-password",
                "jigar@example.com"
        );
        when(passwordEncoder.encode("plain-password")).thenReturn("encoded-password");
        when(userRepository.save(org.mockito.ArgumentMatchers.any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        userService.saveUser(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User user = userCaptor.getValue();
        assertThat(user.getId()).isNull();
        assertThat(user.getUsername()).isEqualTo("jigar");
        assertThat(user.getPassword()).isEqualTo("encoded-password");
        assertThat(user.getEmail()).isEqualTo("jigar@example.com");
        assertThat(user.getCreatedAt()).isNull();
        assertThat(user.getUpdatedAt()).isNull();
        assertThat(user.getGitHubID()).isNull();
    }

    @Test
    void tokenGenerationUsesPersistedUserWithDatabaseId() {
        User persistedUser = new User();
        persistedUser.setId(42L);
        persistedUser.setUsername("jigar");
        persistedUser.setPassword("encoded-password");
        persistedUser.setEmail("jigar@example.com");

        when(userRepository.findByUsername("jigar")).thenReturn(Optional.of(persistedUser));
        when(jwtUtil.generateToken(persistedUser)).thenReturn("jwt-token");
        when(jwtUtil.getExpirationDate("jwt-token")).thenReturn(new Date(1000L));

        userService.generateToken("jigar");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(jwtUtil).generateToken(userCaptor.capture());
        assertThat(userCaptor.getValue().getId()).isEqualTo(42L);
        assertThat(userCaptor.getValue().getUsername()).isEqualTo("jigar");
    }
}
