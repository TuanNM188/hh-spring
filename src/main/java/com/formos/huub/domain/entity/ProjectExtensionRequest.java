package com.formos.huub.domain.entity;

import com.formos.huub.domain.enums.ProjectExtensionRequestStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "project_extension_request")
@Getter
@Setter
@SQLRestriction("is_delete='false'")
@SQLDelete(sql = "UPDATE project_extension_request SET is_delete = true WHERE id = ?")
public class ProjectExtensionRequest extends AbstractAuditingEntity<UUID> {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", length = 36)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", referencedColumnName = "id")
    private Project project;

    @Column(name = "new_completion_date")
    private Instant newCompletionDate;

    @Column(name = "request_explanation", columnDefinition = "TEXT")
    private String requestExplanation;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ProjectExtensionRequestStatus status;
}
