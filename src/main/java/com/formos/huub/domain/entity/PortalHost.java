package com.formos.huub.domain.entity;

import com.formos.huub.domain.enums.PortalHostStatusEnum;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "portal_host")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("is_delete='false'")
@SQLDelete(sql = "UPDATE portal_host SET is_delete = true WHERE id = ?")
public class PortalHost  extends AbstractAuditingEntity<UUID> {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", length = 36)
    private UUID id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    private String email;

    @Column(name = "is_primary")
    private Boolean isPrimary;

    @Column(name = "user_id", length = 36)
    private UUID userId;

    @Column(name = "invite_token")
    private String inviteToken;

    @Column(name = "invite_expire")
    private Instant inviteExpire;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private PortalHostStatusEnum status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portal_id", referencedColumnName = "id")
    private Portal portal;
}
