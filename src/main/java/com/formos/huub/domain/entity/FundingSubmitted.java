package com.formos.huub.domain.entity;

import com.formos.huub.domain.enums.StatusEnum;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

@Entity
@Table(name = "funding_submitted")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("is_delete='false'")
@SQLDelete(sql = "UPDATE funding_submitted SET is_delete = true WHERE id = ?")
public class FundingSubmitted extends AbstractAuditingEntity<UUID> {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", length = 36)
    private UUID id;

    @Column(name = "user_id", length = 36)
    private UUID userId;

    @Column(name = "funding_id", length = 36)
    private UUID fundingId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private StatusEnum status;
}
