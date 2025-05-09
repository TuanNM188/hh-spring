package com.formos.huub.domain.entity;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "project_report")
@Getter
@Setter
@SQLRestriction("is_delete='false'")
@SQLDelete(sql = "UPDATE project_report SET is_delete = true WHERE id = ?")
public class ProjectReport extends AbstractAuditingEntity<UUID> {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", length = 36)
    private UUID id;

    @Column(name = "hours_completed")
    private Integer hoursCompleted;

    @Column(name = "description")
    private String description;

    @Column(name = "service_outcomes")
    private String serviceOutcomes;

    @Column(name = "feedback")
    private String feedback;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", referencedColumnName = "id")
    private Project project;

    @Column(name = "confirmation", columnDefinition = "boolean default true")
    private Boolean confirmation;

    @Column(name = "pdf_url")
    private String pdfUrl;

    @Column(name = "pdf_filename")
    private String pdfFilename;

    @Column(name = "report_number", nullable = false, unique = true)
    private String reportNumber;
}
