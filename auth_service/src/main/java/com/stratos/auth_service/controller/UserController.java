package com.stratos.auth_service.controller;

import com.stratos.auth_service.dto.JWTTokenResponseDTO;
import com.stratos.auth_service.dto.LoginRequestDTO;
import com.stratos.auth_service.dto.RegisterUserRequestDTO;
import com.stratos.auth_service.dto.UserDTO;
import com.stratos.auth_service.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class UserController {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    public UserController(UserService userService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/register-user")
    public ResponseEntity<UserDTO> registerUser(@RequestBody RegisterUserRequestDTO request) {
        UserDTO userDTO = userService.saveUser(request);
         return new ResponseEntity<>(userDTO, HttpStatus.CREATED);
    }

    @PostMapping("/generate-token")
    public ResponseEntity<JWTTokenResponseDTO> generateToken(@RequestBody LoginRequestDTO request) {
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

            if (authentication.isAuthenticated()) {
                JWTTokenResponseDTO jwtTokenResponseDTO = userService.generateToken(request.getUsername());
                return new ResponseEntity<>(jwtTokenResponseDTO, HttpStatus.OK);
            }
            else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        }
        catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }
}
