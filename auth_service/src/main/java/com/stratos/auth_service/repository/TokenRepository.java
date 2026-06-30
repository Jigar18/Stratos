package com.stratos.auth_service.repository;

import com.stratos.auth_service.model.Token;
import com.stratos.auth_service.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {
    Optional<Token> findByRefreshToken(String refreshToken);

    Optional<Token> findByUser(User user);
}
