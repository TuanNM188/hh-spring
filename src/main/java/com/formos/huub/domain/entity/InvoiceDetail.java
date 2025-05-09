/**
 * ***************************************************
 * * Description :
 * * File        : InvoiceDetail
 * * Author      : Hung Tran
 * * Date        : Mar 03, 2025
 * ***************************************************
 **/
package com.formos.huub.domain.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "invoice_detail")
public class InvoiceDetail extends AbstractAuditingEntity<UUID> {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", length = 36)
    private UUID id;

    @Column(name = "item_name", nullable = false)
    private String itemName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "total_appointment_hour", nullable = false)
    private Integer totalAppointmentHours;

    @Column(name = "total_project_hour", nullable = false)
    private Integer totalProjectHours;

    @Column(name = "total_hour", nullable = false)
    private Integer totalHours;

    @Column(name = "price", nullable = false)
    private Double price;

    @Column(name = "amount", nullable = false)
    private Double amount;

    @Column(name = "is_appointment")
    private Boolean isAppointment;

    @Column(name = "service_period_start")
    private Instant servicePeriodStart;

    @Column(name = "service_period_end")
    private Instant servicePeriodEnd;

    @Column(name = "technical_assistance_submit_id", length = 36)
    private UUID technicalAssistanceSubmitId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;
}
