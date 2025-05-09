/**
 * ***************************************************
 * * Description :
 * * File        : Invoice
 * * Author      : Hung Tran
 * * Date        : Mar 03, 2025
 * ***************************************************
 **/
package com.formos.huub.domain.entity;

import com.formos.huub.domain.enums.ProjectStatusEnum;
import com.formos.huub.domain.response.metricsreport.ResponseInvoicedAmountByAdvisor;
import com.formos.huub.domain.response.project.ResponseSearchProject;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "invoice")
@SqlResultSetMapping(
    name = "search_invoice_amount_by_advisor",
    classes = @ConstructorResult(
        targetClass = ResponseInvoicedAmountByAdvisor.class,
        columns = {
            @ColumnResult(name = "id", type = UUID.class),
            @ColumnResult(name = "invoiceDate", type = Instant.class),
            @ColumnResult(name = "advisorId", type = UUID.class),
            @ColumnResult(name = "advisorName", type = String.class),
            @ColumnResult(name = "appointmentHours", type = Float.class),
            @ColumnResult(name = "projectHours", type = Float.class),
            @ColumnResult(name = "totalAmount", type = BigDecimal.class),
            @ColumnResult(name = "filePath", type = String.class),
            @ColumnResult(name = "fileName", type = String.class)
        }
    )
)
public class Invoice extends AbstractAuditingEntity<UUID> {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", length = 36)
    private UUID id;

    @Column(name = "invoice_number", nullable = false, unique = true)
    private String invoiceNumber;

    @Column(name = "due_date", nullable = false)
    private Instant dueDate;

    @Column(name = "bill_to_name", nullable = false)
    private String billToName;

    @Column(name = "bill_to_address")
    private String billToAddress;

    @Column(name = "bill_to_city")
    private String billToCity;

    @Column(name = "bill_to_state")
    private String billToState;

    @Column(name = "bill_to_zip")
    private String billToZip;

    @Column(name = "bill_to_phone")
    private String billToPhone;

    @Column(name = "pay_to_name", nullable = false)
    private String payToName;

    @Column(name = "pay_to_address")
    private String payToAddress;

    @Column(name = "pay_to_city")
    private String payToCity;

    @Column(name = "pay_to_state")
    private String payToState;

    @Column(name = "pay_to_zip")
    private String payToZip;

    @Column(name = "pay_to_phone")
    private String payToPhone;

    @Column(name = "total_amount", nullable = false)
    private Double totalAmount;

    @Column(name = "portal_id", length = 36)
    private UUID portalId;

    @Column(name = "technical_advisor_id", length = 36)
    private UUID technicalAdvisorId;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "invoice_month")
    private String invoiceMonth; // Format: YYYY-MM

    @Builder.Default
    @OneToMany(mappedBy = "invoice")
    private Set<InvoiceDetail> invoiceDetails = new HashSet<>();
}
