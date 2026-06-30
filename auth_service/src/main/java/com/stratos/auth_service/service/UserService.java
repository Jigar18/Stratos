package com.stratos.auth_service.service;

import com.stratos.auth_service.dto.JWTTokenResponseDTO;
import com.stratos.auth_service.dto.RegisterUserRequestDTO;
import com.stratos.auth_service.dto.UserDTO;
import com.stratos.auth_service.model.User;
import com.stratos.auth_service.repository.UserRepository;
import com.stratos.auth_service.util.JWTUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JWTUtil jwtUtil, RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
    }

    public UserDTO saveUser(RegisterUserRequestDTO request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());

        User savedUser = userRepository.save(user);
        return new UserDTO(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail()
        );
    }

    public JWTTokenResponseDTO provideGeneratedToken(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        String token = jwtUtil.generateToken(user);
        JWTTokenResponseDTO jwtTokenResponseDTO = new JWTTokenResponseDTO();
        jwtTokenResponseDTO.setToken(token);
        jwtTokenResponseDTO.setType("Bearer");
        jwtTokenResponseDTO.setExpiresAt(jwtUtil.getExpirationDate(token).toString());
        return jwtTokenResponseDTO;
    }

    public String provideRefreshToken(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return refreshTokenService.createOrReplaceRefreshToken(user);
    }

    public JWTTokenResponseDTO refreshJwtToken(String refreshToken) {
        User user = refreshTokenService.validateRefreshToken(refreshToken);
        String token = jwtUtil.generateToken(user);
        JWTTokenResponseDTO jwtTokenResponseDTO = new JWTTokenResponseDTO();
        jwtTokenResponseDTO.setToken(token);
        jwtTokenResponseDTO.setType("Bearer");
        jwtTokenResponseDTO.setExpiresAt(jwtUtil.getExpirationDate(token).toString());
        return jwtTokenResponseDTO;
    }

    public String rotateRefreshToken(String refreshToken) {
        User user = refreshTokenService.validateRefreshToken(refreshToken);
        return refreshTokenService.createOrReplaceRefreshToken(user);
    }

    public void revokeRefreshToken(String refreshToken) {
        refreshTokenService.revokeRefreshToken(refreshToken);
    }
}
