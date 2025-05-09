package com.formos.huub.domain.entity;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "appointment_detail")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("is_delete='false'")
@SQLDelete(sql = "UPDATE appointment_detail SET is_delete = true WHERE id = ?")
public class AppointmentDetail extends AbstractAuditingEntity<UUID> {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", length = 36)
    private UUID id;

    @Column(name = "category_id", length = 36)
    private UUID categoryId;

    @Column(name = "service_id", length = 36)
    private UUID serviceId;

    @Column(name = "support_description")
    private String supportDescription;

    @Column(name = "share_links")
    private String shareLinks;

    @Column(name = "service_outcomes")
    private String serviceOutcomes;

    @Column(name = "rating", columnDefinition = "integer default 0")
    private Integer rating;

    @Column(name = "feedback")
    private String feedback;

    @Column(name = "comments")
    private String comments;

    @Column(name = "use_award_hours", columnDefinition = "float default 0")
    private Float useAwardHours;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", referencedColumnName = "id")
    private Appointment appointment;
}
