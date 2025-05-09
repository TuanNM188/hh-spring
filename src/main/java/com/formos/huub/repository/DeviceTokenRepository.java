package com.formos.huub.repository;

import com.formos.huub.domain.entity.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeviceTokenRepository extends JpaRepository<DeviceToken, UUID> {


    Optional<DeviceToken> findByTokenAndUserId(String token, UUID userId);

    @Modifying
    @Query("DELETE FROM DeviceToken d WHERE d.userId = :id and d.token = :deviceToken")
    void deleteTokenDevice(UUID id, String deviceToken);
}
