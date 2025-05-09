package com.formos.huub.domain.entity;

import com.formos.huub.domain.enums.SyncEventStatusEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "log_cron_job_entries")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LogCronJobEntries {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", length = 36)
    private UUID id;

    @Column(name = "method_name")
    private String methodName;

    @Column(name = "service_name")
    private String serviceName;

    @Enumerated(EnumType.STRING)
    @Column(name = "log_status", length = 50)
    private SyncEventStatusEnum logStatus;

    @Column(name = "problem", length = 2000)
    private String problem;

    @Column(name = "timestamp")
    private Instant timestamp;

}
