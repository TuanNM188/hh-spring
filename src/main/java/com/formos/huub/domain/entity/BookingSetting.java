package com.formos.huub.domain.entity;

import com.formos.huub.domain.enums.MeetingPlatformEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "booking_setting")
@Getter
@Setter
@SQLRestriction("is_delete='false'")
@SQLDelete(sql = "UPDATE booking_setting SET is_delete = true WHERE id = ?")
public class BookingSetting extends AbstractAuditingEntity<UUID> {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", length = 36)
    private UUID id;

    @Column(name = "earliest_date")
    private Instant earliestDate;

    @Column(name = "timezone", length = 50)
    private String timezone;

    @Enumerated(EnumType.STRING)
    @Column(name = "meeting_platform", length = 50)
    private MeetingPlatformEnum meetingPlatform;

    @Column(name = "link_meeting_platform", length = 2000)
    private String linkMeetingPlatform;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @OneToMany(mappedBy = "bookingSetting")
    private Set<Availability> availabilities = new HashSet<>();

    @OneToOne(mappedBy = "bookingSetting", cascade = { CascadeType.REMOVE, CascadeType.PERSIST })
    private CalendarIntegration calendarIntegration;
}
