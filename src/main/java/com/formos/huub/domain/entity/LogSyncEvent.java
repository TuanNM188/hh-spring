package com.formos.huub.domain.entity;

import com.formos.huub.domain.enums.IntegrateByEnum;
import com.formos.huub.domain.enums.SyncEventStatusEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "log_sync_event")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LogSyncEvent {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", length = 36)
    private UUID id;

    @Column(name = "calendar_integration_id")
    private UUID calendarIntegrationId;

    @Column(name = "url", length = 2000)
    private String url;

    @Column(name = "problem", length = 2000)
    private String problem;

    @Column(name = "timestamp")
    private Instant timestamp;

    @Column(name = "portal_id")
    private UUID portalId;

    @Enumerated(EnumType.STRING)
    @Column(name = "integrate_by", length = 50)
    private IntegrateByEnum integrateBy;

}
