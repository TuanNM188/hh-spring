/**
 * ***************************************************
 * * Description :
 * * File        : Project
 * * Author      : Hung Tran
 * * Date        : Jan 20, 2025
 * ***************************************************
 **/
package com.formos.huub.domain.entity;

import com.formos.huub.domain.enums.AppointmentStatusEnum;
import com.formos.huub.domain.enums.ProjectStatusEnum;
import com.formos.huub.domain.response.appointment.ResponseSearchAppointment;
import com.formos.huub.domain.response.project.ResponseSearchProject;
import com.formos.huub.domain.response.tasurvey.ResponseTaSurveyProject;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "project")
@Getter
@Setter
@SQLRestriction("is_delete='false'")
@SQLDelete(sql = "UPDATE project SET is_delete = true WHERE id = ?")
@SqlResultSetMappings({
    @SqlResultSetMapping(
        name = "search_projects",
        classes = @ConstructorResult(
            targetClass = ResponseSearchProject.class,
            columns = {
                @ColumnResult(name = "id", type = UUID.class),
                @ColumnResult(name = "projectName", type = String.class),
                @ColumnResult(name = "estimatedHoursNeeded", type = Float.class),
                @ColumnResult(name = "completedDate", type = Instant.class),
                @ColumnResult(name = "businessOwnerId", type = UUID.class),
                @ColumnResult(name = "businessOwnerName", type = String.class),
                @ColumnResult(name = "advisorId", type = UUID.class),
                @ColumnResult(name = "advisorName", type = String.class),
                @ColumnResult(name = "navigatorId", type = UUID.class),
                @ColumnResult(name = "navigatorName", type = String.class),
                @ColumnResult(name = "status", type = ProjectStatusEnum.class),
                @ColumnResult(name = "requestDate", type = Instant.class),
                @ColumnResult(name = "estimatedCompletionDate", type = Instant.class),
            }
        )
    ),@SqlResultSetMapping(
        name = "search_ta_survey_projects",
        classes = @ConstructorResult(
            targetClass = ResponseTaSurveyProject.class,
            columns = {
                @ColumnResult(name = "projectId", type = UUID.class),
                @ColumnResult(name = "projectName", type = String.class),
                @ColumnResult(name = "businessOwnerId", type = UUID.class),
                @ColumnResult(name = "businessOwnerName", type = String.class),
                @ColumnResult(name = "businessOwnerEmail", type = String.class),
                @ColumnResult(name = "advisorId", type = UUID.class),
                @ColumnResult(name = "advisorName", type = String.class),
                @ColumnResult(name = "vendorName", type = String.class),
                @ColumnResult(name = "rating", type = Integer.class),
                @ColumnResult(name = "feedback", type = String.class),
            }
        )
    )
})
public class Project extends AbstractAuditingEntity<UUID> {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", length = 36)
    private UUID id;

    @Column(name = "project_name", nullable = false)
    private String projectName;

    @Column(name = "scope_of_work", columnDefinition = "TEXT")
    private String scopeOfWork;

    // Feedback about the Project done by Business Owner
    @Column(name = "rating", nullable = false, columnDefinition = "integer default 0")
    private Integer rating;

    // Question: Was the work delivered as expected?
    @Column(name = "work_as_expected", nullable = false, columnDefinition = "boolean default false")
    private Boolean workAsExpected;

    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;

    @Column(name = "completed_date", columnDefinition = "TEXT")
    private Instant completedDate;

    @Column(name = "work_in_progress_date")
    private Instant workInProgressDate;

    @Column(name = "denied_date")
    private Instant deniedDate;

    @Column(name = "remaining_award_hours")
    private Float remainingAwardHours;

    //---------------------------------------------------
    // User is Business Owner
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_owner_id", referencedColumnName = "id")
    private BusinessOwner businessOwner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technical_assistance_submit_id", referencedColumnName = "id")
    private TechnicalAssistanceSubmit technicalAssistanceSubmit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portal_id", referencedColumnName = "id")
    private Portal portal;

    // Community Partner is Vendor = true
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", referencedColumnName = "id")
    private CommunityPartner vendor;

    // User is Technical Advisor
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technical_advisor_id", referencedColumnName = "id")
    private TechnicalAdvisor technicalAdvisor;

    @Column(name = "estimated_hours_needed", nullable = false, columnDefinition = "float default 0")
    private Float estimatedHoursNeeded;

    @Column(name = "proposed_start_date")
    private Instant proposedStartDate;

    @Column(name = "estimated_completion_date")
    private Instant estimatedCompletionDate;

    @Column(name = "category_id", length = 36)
    private UUID categoryId;

    @Column(name = "service_id", length = 36)
    private UUID serviceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private ProjectStatusEnum status;

    @OneToOne(mappedBy = "project")
    private ProjectReport projectReport;

    @OneToMany(mappedBy = "project")
    private Set<ProjectUpdate> projectUpdates;

    @OneToMany(mappedBy = "project")
    private Set<ProjectExtensionRequest> projectExtensionRequests;

    @Column(name = "is_additional_work", nullable = false, columnDefinition = "boolean default false")
    private Boolean isAdditionalWork;

    @Column(name="appointment_id")
    private UUID appointmentId;

    @Column(name = "invoice_id")
    private UUID invoiceId;
}
