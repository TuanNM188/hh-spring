package com.formos.huub.domain.entity;

import com.formos.huub.domain.enums.AppointmentStatusEnum;
import com.formos.huub.domain.enums.EventRegistrationStatusEnum;
import com.formos.huub.domain.response.appointment.ResponseSearchAppointment;
import com.formos.huub.domain.response.eventregistration.ResponseSearchEventRegistration;
import com.formos.huub.domain.response.tasurvey.ResponseTaSurveyAppointment;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "event_registrations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("is_delete='false'")
@SQLDelete(sql = "UPDATE event_registrations SET is_delete = true WHERE id = ?")
@SqlResultSetMapping(
    name = "search_event_registration_managements",
    classes = @ConstructorResult(
        targetClass = ResponseSearchEventRegistration.class,
        columns = {
            @ColumnResult(name = "id", type = UUID.class),
            @ColumnResult(name = "registrationDate", type = Instant.class),
            @ColumnResult(name = "startTime", type = Instant.class),
            @ColumnResult(name = "subject", type = String.class),
            @ColumnResult(name = "businessOwnerName", type = String.class),
            @ColumnResult(name = "portalName", type = String.class)
        }
    )
)
public class EventRegistration extends AbstractAuditingEntity<UUID> {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", length = 36)
    private UUID id;

    @Column(name = "business_owner_id", length = 36)
    private UUID businessOwnerId;

    @Column(name = "calendar_event_id", length = 36)
    private UUID calendarEventId;

    @Column(name = "registration_date")
    private Instant registrationDate;

    @Column(name = "registration_status")
    @Enumerated(EnumType.STRING)
    private EventRegistrationStatusEnum registrationStatus;

    @Column(name = "external_event_id")
    private String externalEventId;

    @Column(name = "external_attendee_id")
    private String externalAttendeeId;

    @Column(name = "checked_in")
    private Boolean isCheckedIn;

    @Column(name = "cancelled")
    private Boolean isCancelled;

    @Column(name = "refunded")
    private Boolean isRefunded;
}
