package com.formos.huub.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.formos.huub.domain.enums.PortalFundingStatusEnum;
import com.formos.huub.domain.enums.PortalFundingTypeEnum;
import com.formos.huub.tracker.TrackTranslate;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "funding")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("is_delete='false'")
@SQLDelete(sql = "UPDATE funding SET is_delete = true WHERE id = ?")
public class Funding extends AbstractAuditingEntity<UUID> {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", length = 36)
    private UUID id;

    @Column(name = "title")
    @TrackTranslate
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private PortalFundingStatusEnum status;

    @Column(name = "date_added")
    private Instant dateAdded;

    @Column(name = "publish_date")
    private Instant publishDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private PortalFundingTypeEnum type;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "description")
    @TrackTranslate
    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "has_deadline")
    private Boolean hasDeadline;

    @Column(name = "application_deadline")
    private Instant applicationDeadline;

    @Column(name = "application_url")
    private String applicationUrl;

    @Column(name = "application_process")
    private String applicationProcess;

    @Column(name = "application_requirement")
    private String applicationRequirement;

    @Column(name = "application_restriction")
    private String applicationRestriction;

    @Column(name = "funding_categories")
    private String fundingCategories;

    @JsonIgnore
    @ManyToMany
    @JoinTable(
        name = "funding_portal",
        joinColumns = { @JoinColumn(name = "funding_id", referencedColumnName = "id") },
        inverseJoinColumns = { @JoinColumn(name = "portal_id", referencedColumnName = "id") }
    )
    @BatchSize(size = 20)
    private Set<Portal> portals = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "funder_id", referencedColumnName = "id")
    private Funder funder;
}
