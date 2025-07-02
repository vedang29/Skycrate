package com.skycrate.backend.skycrateBackend.repository;

import com.skycrate.backend.skycrateBackend.entity.RefreshToken;
import com.skycrate.backend.skycrateBackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByUser(User user);
}