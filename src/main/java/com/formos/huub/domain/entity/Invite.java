package com.formos.huub.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "invite")
public class Invite extends AbstractAuditingEntity<UUID> {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", length = 36)
    private UUID id;

    @Email
    @Column(length = 100, unique = true, nullable = false)
    private String email;

    @Column(name = "role_name", length = 50, nullable = false)
    private String roleName;

    @Column(name = "invite_token")
    private String inviteToken;;

    @Column(name = "expires_at")
    private Instant expiresAt = null;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "attributes", columnDefinition = "jsonb")
    private String attributes;

}
