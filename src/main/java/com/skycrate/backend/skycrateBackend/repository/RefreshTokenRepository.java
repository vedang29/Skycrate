package com.skycrate.backend.skycrateBackend.repository;

import com.skycrate.backend.skycrateBackend.entity.RefreshToken;
import com.skycrate.backend.skycrateBackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    @Transactional
    @Modifying
    @Query("DELETE FROM RefreshToken t WHERE t.user = :user")
    void deleteByUser(User user);
}