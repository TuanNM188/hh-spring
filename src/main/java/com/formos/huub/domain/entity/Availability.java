package com.formos.huub.domain.entity;

import com.formos.huub.domain.enums.DaysOfWeek;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "availability")
@Getter
@Setter
@SQLRestriction("is_delete='false'")
@SQLDelete(sql = "UPDATE availability SET is_delete = true WHERE id = ?")
public class Availability extends AbstractAuditingEntity<UUID> {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", length = 36)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "days_of_week", length = 50)
    private DaysOfWeek daysOfWeek;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "is_active")
    private Boolean isActive;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_setting_id", referencedColumnName = "id")
    private BookingSetting bookingSetting;
}
