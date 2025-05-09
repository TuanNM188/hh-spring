package com.formos.huub.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "terms_acceptance")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("is_delete='false'")
@SQLDelete(sql = "UPDATE terms_acceptance SET is_delete = true WHERE id = ?")
public class TermsAcceptance extends AbstractAuditingEntity<UUID> {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", length = 36)
    private UUID id;

    @Column(name = "acceptance_date")
    private Instant acceptanceDate;

    @Column(name = "pdf_url")
    private String pdfUrl;

    @Column(name = "version_number")
    private Integer versionNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "learning_library_id", referencedColumnName = "id")
    private LearningLibrary learningLibrary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "terms_and_conditions_id", referencedColumnName = "id")
    private TermsAndConditions termsAndConditions;

}
