package com.formos.huub.domain.entity;

import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.*;

import java.util.UUID;

@Entity
@Table(name = "organization")
@Getter
@Setter
@SQLRestriction("is_delete='false'")
@SQLDelete(sql = "UPDATE organization SET is_delete = true WHERE id = ?")
public class Organization extends AbstractAuditingEntity<UUID> {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", length = 36)
    private UUID id;

    @Column(nullable = false)
    private String organizationName;

    private String description;

    private String address;

    @Column(nullable = false)
    private String contactEmail;

    private String contactPhone;

}
