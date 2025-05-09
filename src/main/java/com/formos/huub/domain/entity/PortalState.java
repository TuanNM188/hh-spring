package com.formos.huub.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

@Entity
@Table(name = "portal_state")
@Getter
@Setter
@SQLRestriction("is_delete='false'")
@SQLDelete(sql = "UPDATE portal_state SET is_delete = true WHERE id = ?")
public class PortalState  extends AbstractAuditingEntity<UUID> {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", length = 36)
    private UUID id;

    @Column(name = "country_code")
    private String countryCode;

    @Column(name = "state_id")
    private String stateId;

    @Column(name = "cities")
    private String cities;

    @Column(name = "zipcodes")
    private String zipcodes;

    @Column(name = "priority_order")
    private Integer priorityOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portal_id", referencedColumnName = "id")
    private Portal portal;
}
