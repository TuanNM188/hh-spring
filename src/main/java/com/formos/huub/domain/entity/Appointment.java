package com.formos.huub.domain.entity;

import com.formos.huub.domain.enums.AppointmentStatusEnum;
import com.formos.huub.domain.response.appointment.ResponseSearchAppointment;
import com.formos.huub.domain.response.tasurvey.ResponseTaSurveyAppointment;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "appointment")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("is_delete='false'")
@SQLDelete(sql = "UPDATE appointment SET is_delete = true WHERE id = ?")
@SqlResultSetMappings(
    {
        @SqlResultSetMapping(
            name = "search_appointments",
            classes = @ConstructorResult(
                targetClass = ResponseSearchAppointment.class,
                columns = {
                    @ColumnResult(name = "id", type = UUID.class),
                    @ColumnResult(name = "appointmentDate", type = Instant.class),
                    @ColumnResult(name = "timezone", type = String.class),
                    @ColumnResult(name = "businessOwnerId", type = UUID.class),
                    @ColumnResult(name = "businessOwnerName", type = String.class),
                    @ColumnResult(name = "advisorId", type = UUID.class),
                    @ColumnResult(name = "advisorName", type = String.class),
                    @ColumnResult(name = "navigatorId", type = UUID.class),
                    @ColumnResult(name = "navigatorName", type = String.class),
                    @ColumnResult(name = "vendorName", type = String.class),
                    @ColumnResult(name = "categoryName", type = String.class),
                    @ColumnResult(name = "serviceName", type = String.class),
                    @ColumnResult(name = "status", type = AppointmentStatusEnum.class),
                }
            )
        ),@SqlResultSetMapping(
            name = "search_ta_survey_appointments",
            classes = @ConstructorResult(
                targetClass = ResponseTaSurveyAppointment.class,
                columns = {
                    @ColumnResult(name = "appointmentId", type = UUID.class),
                    @ColumnResult(name = "appointmentDate", type = Instant.class),
                    @ColumnResult(name = "timezone", type = String.class),
                    @ColumnResult(name = "businessOwnerId", type = UUID.class),
                    @ColumnResult(name = "businessOwnerName", type = String.class),
                    @ColumnResult(name = "advisorId", type = UUID.class),
                    @ColumnResult(name = "advisorName", type = String.class),
                    @ColumnResult(name = "businessOwnerEmail", type = String.class),
                    @ColumnResult(name = "vendorName", type = String.class),
                    @ColumnResult(name = "rating", type = Integer.class),
                    @ColumnResult(name = "feedback", type = String.class),
                }
            )
        )
    }
)
public class Appointment extends AbstractAuditingEntity<UUID> {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", length = 36)
    private UUID id;

    @Column(name = "appointment_date")
    private Instant appointmentDate;

    @Column(name = "timezone")
    private String timezone;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private AppointmentStatusEnum status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technical_assistance_submit_id", referencedColumnName = "id")
    private TechnicalAssistanceSubmit technicalAssistanceSubmit;

    // User is Business Owner
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technical_advisor_id", referencedColumnName = "id")
    private TechnicalAdvisor technicalAdvisor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "community_partner_id", referencedColumnName = "id")
    private CommunityPartner communityPartner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portal_id", referencedColumnName = "id")
    private Portal portal;

    @OneToOne(mappedBy = "appointment")
    private AppointmentDetail appointmentDetail;

    @OneToOne(mappedBy = "appointment")
    private AppointmentReport appointmentReport;

    @Column(name = "invoice_id")
    private UUID invoiceId;
}
