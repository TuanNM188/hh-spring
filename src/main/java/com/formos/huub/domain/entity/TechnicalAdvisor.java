package com.formos.huub.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.formos.huub.domain.enums.IndustriesServedEnum;
import com.formos.huub.domain.enums.LanguagesSpokenEnum;
import com.formos.huub.domain.enums.ToolsUsedEnum;
import com.formos.huub.domain.enums.UserStatusEnum;
import com.formos.huub.domain.provider.UserProvider;
import com.formos.huub.domain.response.directmessage.ResponseSearchReferralMessage;
import com.formos.huub.domain.response.technicaladvisor.ResponseTechnicalAdvisor;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "technical_advisor")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("is_delete='false'")
@SQLDelete(sql = "UPDATE technical_advisor SET is_delete = true WHERE id = ?")
@SqlResultSetMapping(
    name = "search_technical_advisors",
    classes = @ConstructorResult(
        targetClass = ResponseTechnicalAdvisor.class,
        columns = {
            @ColumnResult(name = "id", type = UUID.class),
            @ColumnResult(name = "normalizedFullName", type = String.class),
            @ColumnResult(name = "email", type = String.class),
            @ColumnResult(name = "organization", type = String.class),
            @ColumnResult(name = "phoneNumber", type = String.class),
            @ColumnResult(name = "status", type = UserStatusEnum.class),
            @ColumnResult(name = "startDate", type = Instant.class),
            @ColumnResult(name = "userId", type = UUID.class)
        }
    )
)
public class TechnicalAdvisor extends AbstractAuditingEntity<UUID> implements UserProvider {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", length = 36)
    private UUID id;

    @Column(name = "specialty")
    private String specialty;

    @Column(name = "education")
    private String education;

    @Column(name = "headshot")
    private String headshot;

    @Column(name = "time_zone", length = 50)
    private String timeZone;

    @Enumerated(EnumType.STRING)
    @Column(name = "languages_spoken", length = 50)
    private LanguagesSpokenEnum languagesSpoken;

    @Enumerated(EnumType.STRING)
    @Column(name = "industries_served", length = 50)
    private IndustriesServedEnum industriesServed;

    @Enumerated(EnumType.STRING)
    @Column(name = "tools_used", length = 50)
    private ToolsUsedEnum toolsUsed;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "community_partner_id", referencedColumnName = "id")
    private CommunityPartner communityPartner;

    @JsonIgnore
    @ManyToMany
    @JoinTable(
        name = "technical_advisor_portal",
        joinColumns = { @JoinColumn(name = "technical_advisor_id", referencedColumnName = "id") },
        inverseJoinColumns = { @JoinColumn(name = "portal_id", referencedColumnName = "id") }
    )
    @BatchSize(size = 20)
    private Set<Portal> portals = new HashSet<>();
}
