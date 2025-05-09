package com.formos.huub.domain.entity;

import com.formos.huub.domain.enums.TermsAndConditionsTypeEnum;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "terms_and_conditions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("is_delete='false'")
@SQLDelete(sql = "UPDATE terms_and_conditions SET is_delete = true WHERE id = ?")
public class TermsAndConditions extends AbstractAuditingEntity<UUID> {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", length = 36)
    private UUID id;

    @Column(name = "text")
    private String text;

    @Column(name = "version_number")
    private Integer versionNumber;

    @Column(name = "is_active")
    private Boolean isActive;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 50)
    private TermsAndConditionsTypeEnum type;

    @OneToMany(mappedBy = "termsAndConditions", fetch = FetchType.LAZY)
    private Set<TermsAcceptance> termsAcceptances = new HashSet<>();

}
