package com.formos.huub.domain.entity;

import com.formos.huub.domain.enums.CalendarStatusEnum;
import com.formos.huub.domain.enums.CalendarTypeEnum;
import com.formos.huub.domain.enums.IntegrateByEnum;
import com.formos.huub.domain.enums.SyncEventStatusEnum;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "calendar_integration")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@SQLRestriction("is_delete='false'")
@SQLDelete(sql = "UPDATE calendar_integration SET is_delete = true WHERE id = ?")
public class CalendarIntegration  extends AbstractAuditingEntity<UUID> {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", length = 36)
    private UUID id;

    @Column(name = "calendar_id")
    private String calendarId;

    @Column(name = "calendar_ref_id")
    private String calendarRefId;

    @Enumerated(EnumType.STRING)
    @Column(name = "calendar_type", length = 50)
    private CalendarTypeEnum calendarType;

    @Column(name = "url", length = 2000)
    private String url;

    @Column(name = "problem", length = 2000)
    private String problem;

    @Column(name = "priority_order")
    private Integer priorityOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "calendar_status", length = 50)
    private CalendarStatusEnum calendarStatus;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "attributes", columnDefinition = "jsonb")
    private String attributes;

    @Enumerated(EnumType.STRING)
    @Column(name = "integrate_by", length = 50)
    private IntegrateByEnum integrateBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "sync_event_status", length = 50)
    private SyncEventStatusEnum syncEventStatus;

    @Column(name = "retry_count")
    private Integer retryCount;

    @Column(name = "last_sync")
    private Instant lastSync;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_setting_id", referencedColumnName = "id")
    private BookingSetting bookingSetting;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portal_id", referencedColumnName = "id")
    private Portal portal;

    @OneToOne(mappedBy = "calendarIntegration", cascade = {CascadeType.REMOVE, CascadeType.PERSIST })
    private CalendarToken calendarIntegrationToken;
}
