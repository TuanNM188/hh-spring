package com.formos.huub.domain.entity;

import com.formos.huub.domain.enums.AccessTypeEnum;
import com.formos.huub.domain.enums.ApprovalStatusEnum;
import com.formos.huub.domain.enums.ContentTypeEnum;
import com.formos.huub.domain.enums.LearningLibraryStatusEnum;
import com.formos.huub.domain.response.learninglibrary.ResponseSearchLearningLibrary;
import com.formos.huub.domain.response.technicalassistance.ResponseSearchApplication;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "technical_assistance_submit")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("is_delete='false'")
@SQLDelete(sql = "UPDATE technical_assistance_submit SET is_delete = true WHERE id = ?")
@SqlResultSetMapping(
    name = "search_applications",
    classes = @ConstructorResult(
        targetClass = ResponseSearchApplication.class,
        columns = {
            @ColumnResult(name = "id", type = UUID.class),
            @ColumnResult(name = "projectId", type = UUID.class),
            @ColumnResult(name = "userId", type = UUID.class),
            @ColumnResult(name = "businessOwnerName", type = String.class),
            @ColumnResult(name = "lastAppointment", type = Instant.class),
            @ColumnResult(name = "upcomingAppointment", type = Instant.class),
            @ColumnResult(name = "projectStatus", type = String.class),
            @ColumnResult(name = "applicationStatus", type = ApprovalStatusEnum.class),
            @ColumnResult(name = "remainingAwardHours", type = Float.class),
            @ColumnResult(name = "platformName", type = String.class),
        }
    )
)
public class TechnicalAssistanceSubmit extends AbstractAuditingEntity<UUID> {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", length = 36)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portal_id", referencedColumnName = "id")
    private Portal portal;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private ApprovalStatusEnum status;

    @Column(name = "submit_at")
    private Instant submitAt;

    @Column(name = "approval_date")
    private Instant approvalDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id", referencedColumnName = "id")
    private User approver;

    @Column(name = "reason")
    private String reason;

    @Column(name = "assign_award_hours")
    private Float assignAwardHours;

    @Column(name = "remaining_award_hours")
    private Float remainingAwardHours;

    @Column(name = "assign_vendor_id")
    private UUID assignVendorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_term_id", referencedColumnName = "id")
    private ProgramTerm programTerm;

}
