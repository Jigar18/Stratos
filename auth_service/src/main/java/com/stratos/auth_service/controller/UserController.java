package com.stratos.auth_service.controller;

import com.stratos.auth_service.dto.UserDTO;
import com.stratos.auth_service.model.User;
import com.stratos.auth_service.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserDTO> registerUser(@RequestBody User user) {
        UserDTO userDTO = userService.saveUser(user);
         return new ResponseEntity<>(userDTO, HttpStatus.CREATED);
    }
}
