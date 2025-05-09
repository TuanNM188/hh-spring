package com.formos.huub.domain.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "token_manager")
public class TokenManager {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", length = 36)
    private UUID id;

    @Column(name = "login", nullable = false)
    private String login;

    @Column(name = "access_token_key", nullable = true, length = 100)
    private String accessTokenKey;

    @Column(name = "access_token", nullable = false, length = 500)
    private String accessToken;

    @Column(name = "refresh_token", nullable = false)
    private String refreshToken;

    @Column(name = "expired_time", nullable = false)
    private Instant expiredTime;

    @Column(name = "device_token", length = 500)
    private String deviceToken;

    @Column(name = "device_name")
    private String deviceName;

    @Column(name = "device_type")
    private String deviceType;

    @Column(name = "device_info", length = 1000)
    private String deviceInfo;
}
