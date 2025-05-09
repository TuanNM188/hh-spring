package com.formos.huub.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "calendar_token")
@Getter
@Setter
@SQLRestriction("is_delete='false'")
@SQLDelete(sql = "UPDATE calendar_token SET is_delete = true WHERE id = ?")
public class CalendarToken extends AbstractAuditingEntity<UUID> {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", length = 36)
    private UUID id;

    private String accessToken;

    private String refreshToken;

    private Instant tokenExpireTime;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "calendar_integration_id", referencedColumnName = "id")
    private CalendarIntegration calendarIntegration;
}
