package com.formos.huub.domain.entity;

import com.formos.huub.domain.enums.PortalActivityTypeEnum;
import com.formos.huub.domain.enums.ProblemTypeEnum;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "portal_activity_log")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@SQLRestriction("is_delete='false'")
@SQLDelete(sql = "UPDATE portal_activity_log SET is_delete = true WHERE id = ?")
public class PortalActivityLog extends AbstractAuditingEntity<UUID> {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", length = 36)
    private UUID id;

    @Column(name = "portal_id", length = 36)
    private UUID portalId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "data", columnDefinition = "jsonb")
    private String data;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type")
    private PortalActivityTypeEnum activityType;

    @Enumerated(EnumType.STRING)
    @Column(name = "problem_type")
    private ProblemTypeEnum problemType;

    @Column(name = "problem")
    private String problem;
}
