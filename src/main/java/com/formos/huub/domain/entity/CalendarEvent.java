package com.formos.huub.domain.entity;

import com.formos.huub.domain.enums.EventStatusEnum;
import com.formos.huub.domain.response.businessowner.ResponseSearchEventRegistrations;
import com.formos.huub.domain.response.calendarevent.ResponseSearchCalendarEvents;
import com.formos.huub.tracker.TrackTranslate;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "calendar_event")
@Getter
@Setter
@SQLRestriction("is_delete='false'")
@SQLDelete(sql = "UPDATE calendar_event SET is_delete = true WHERE id = ?")
@SqlResultSetMappings(
    {
        @SqlResultSetMapping(
            name = "search_calendar_event",
            classes = @ConstructorResult(
                targetClass = ResponseSearchCalendarEvents.class,
                columns = {
                    @ColumnResult(name = "id", type = UUID.class),
                    @ColumnResult(name = "subject", type = String.class),
                    @ColumnResult(name = "startTime", type = Instant.class),
                    @ColumnResult(name = "endTime", type = Instant.class),
                    @ColumnResult(name = "imageUrl", type = String.class),
                    @ColumnResult(name = "website", type = String.class),
                    @ColumnResult(name = "createdBy", type = String.class),
                    @ColumnResult(name = "description", type = String.class),
                    @ColumnResult(name = "initBy", type = String.class),
                    @ColumnResult(name = "status", type = EventStatusEnum.class),
                }
            )
        ),
        @SqlResultSetMapping(
            name = "search_event_registrations",
            classes = @ConstructorResult(
                targetClass = ResponseSearchEventRegistrations.class,
                columns = {
                    @ColumnResult(name = "calendarEventId", type = UUID.class),
                    @ColumnResult(name = "eventName", type = String.class),
                    @ColumnResult(name = "eventDate", type = String.class),
                }
            )
        ),
    }
)
public class CalendarEvent extends AbstractAuditingEntity<UUID> {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", length = 36)
    private UUID id;

    @Column(name = "external_event_id")
    private String externalEventId;

    @Column(name = "external_calendar_id")
    private String externalCalendarId;

    @Column(name = "subject")
    @TrackTranslate
    private String subject;

    @Column(name = "body")
    private String body;

    @Column(name = "start_time")
    private Instant startTime;

    @Column(name = "end_time")
    private Instant endTime;

    @Column(name = "timezone")
    private String timezone;

    @Column(name = "location")
    private String location;

    @Column(name = "summary")
    private String summary;

    @Column(name = "description")
    @TrackTranslate
    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "organizer_name")
    @TrackTranslate
    private String organizerName;

    @Column(name = "website")
    private String website;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private EventStatusEnum status;

    @Column(name = "init_by")
    private String initBy;

    @Column(name = "checksum")
    private String checksum;

    @Column(name = "is_huub_event")
    private boolean isHuubEvent;

    @ManyToMany
    @JoinTable(
        name = "portal_calendar_event",
        joinColumns = { @JoinColumn(name = "calendar_event_id", referencedColumnName = "id") },
        inverseJoinColumns = { @JoinColumn(name = "portal_id", referencedColumnName = "id") }
    )
    @BatchSize(size = 20)
    private Set<Portal> portals = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "calendar_integration_id", referencedColumnName = "id")
    private CalendarIntegration calendarIntegration;
}
