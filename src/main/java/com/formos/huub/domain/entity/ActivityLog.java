package com.formos.huub.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "activity_log")
@SQLRestriction("is_delete='false'")
@SQLDelete(sql = "UPDATE activity_log SET is_delete = true WHERE id = ?")
public class ActivityLog extends AbstractAuditingEntity<UUID>{

    @Id
    @GeneratedValue
    @Column(name = "id", length = 36)
    private UUID id;

    @Column(name = "login", nullable = false)
    private String login;

    @Column(name = "activity_type")
    private String activityType;

    @Column(name = "device_name")
    private String deviceName;

    @Column(name = "device_type")
    private String deviceType;

    @Column(name = "device_info", length = 1000)
    private String deviceInfo;

    @Column(name = "operating_system", length = 100)
    private String operatingSystem;

    @Column(name = "browser", length = 100)
    private String browser;

    @Column(name = "browser_version", length = 50)
    private String browserVersion;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "additional_info", length = 1000)
    private String additionalInfo;

    @Column(name = "note", length = 1000)
    private String note;

    @Column(name = "access_token", columnDefinition = "TEXT", length = 1000)
    private String accessToken;

    @Column(name = "refresh_token", columnDefinition = "TEXT", length = 1000)
    private String refreshToken;
}
