package com.formos.huub.repository;

import com.formos.huub.domain.entity.TokenManager;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenManagerRepository extends JpaRepository<TokenManager, UUID> {
    List<TokenManager> findByLogin(final String login);

    Optional<TokenManager> findByRefreshToken(final String refreshToken);

    List<TokenManager> findByLoginAndDeviceToken(final String login, final String deviceToken);

    @Modifying
    int deleteByLogin(final String login);

    int deleteByLoginAndDeviceToken(final String login, String deviceToken);

    boolean existsByAccessTokenKey(String accessTokenKey);
}
