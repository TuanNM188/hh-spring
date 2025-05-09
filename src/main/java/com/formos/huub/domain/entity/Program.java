package com.formos.huub.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
@Entity
@Table(name = "program")
@Getter
@Setter
@SQLRestriction("is_delete='false'")
@SQLDelete(sql = "UPDATE program SET is_delete = true WHERE id = ?")
public class Program extends AbstractAuditingEntity<UUID> {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", length = 36)
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "contract_start")
    private Instant contractStart;

    @Column(name = "contract_end")
    private Instant contractEnd;

    @Column(name = "is_active")
    private Boolean isActive;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "portal_id", referencedColumnName = "id")
    private Portal portal;

    @OneToMany(mappedBy = "program")
    private Set<ProgramTerm> programTerms;
}
