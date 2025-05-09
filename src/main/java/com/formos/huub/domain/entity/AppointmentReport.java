package com.formos.huub.domain.entity;

import com.formos.huub.domain.response.metricsreport.ResponseSearchAppointmentProjectReport;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "appointment_report")
@Getter
@Setter
@SQLRestriction("is_delete='false'")
@SQLDelete(sql = "UPDATE appointment_report SET is_delete = true WHERE id = ?")
@SqlResultSetMappings(
    {
        @SqlResultSetMapping(
            name = "search_appointment_project_report",
            classes = @ConstructorResult(
                targetClass = ResponseSearchAppointmentProjectReport.class,
                columns = {
                    @ColumnResult(name = "id", type = UUID.class),
                    @ColumnResult(name = "reportSubmissionDate", type = Instant.class),
                    @ColumnResult(name = "invoiceNumber", type = String.class),
                    @ColumnResult(name = "businessOwnerName", type = String.class),
                    @ColumnResult(name = "advisorName", type = String.class),
                    @ColumnResult(name = "hours", type = Float.class),
                    @ColumnResult(name = "pdfUrl", type = String.class),
                    @ColumnResult(name = "pdfFilename", type = String.class),
                }
            )
        ),
    }
)
public class AppointmentReport extends AbstractAuditingEntity<UUID> {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", length = 36)
    private UUID id;

    @Column(name = "description")
    private String description;

    @Column(name = "service_outcomes")
    private String serviceOutcomes;

    @Column(name = "feedback")
    private String feedback;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", referencedColumnName = "id")
    private Appointment appointment;

    @Column(name = "business_owner_attended", columnDefinition = "boolean default true")
    private Boolean businessOwnerAttended;

    @Column(name = "pdf_url")
    private String pdfUrl;

    @Column(name = "pdf_filename")
    private String pdfFilename;

    @Column(name = "report_number", nullable = false, unique = true)
    private String reportNumber;
}
