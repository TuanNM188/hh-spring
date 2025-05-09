package com.formos.huub.domain.entity;

import com.formos.huub.domain.enums.ApprovalStatusEnum;
import com.formos.huub.domain.enums.StatusEnum;
import com.formos.huub.domain.response.technicalassistance.ResponseSearchApplication;
import com.formos.huub.domain.response.vendor.ResponseSearchVendors;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "program_term_vendor")
@Getter
@Setter
@SQLRestriction("is_delete='false'")
@SQLDelete(sql = "UPDATE program_term_vendor SET is_delete = true WHERE id = ?")
@SqlResultSetMapping(
    name = "search_vendors",
    classes = @ConstructorResult(
        targetClass = ResponseSearchVendors.class,
        columns = {
            @ColumnResult(name = "id", type = UUID.class),
            @ColumnResult(name = "communityPartnerId", type = UUID.class),
            @ColumnResult(name = "communityPartnerName", type = String.class),
            @ColumnResult(name = "navigatorId", type = UUID.class),
            @ColumnResult(name = "navigatorName", type = String.class),
            @ColumnResult(name = "totalAssigns", type = Integer.class),
            @ColumnResult(name = "budgetAssign", type = BigDecimal.class),
            @ColumnResult(name = "budgetInProgress", type = BigDecimal.class),
            @ColumnResult(name = "budgetCompleted", type = BigDecimal.class),
        }
    )
)
public class  ProgramTermVendor extends AbstractAuditingEntity<UUID> {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", length = 36)
    private UUID id;

    @Column(name = "vendor_id", length = 36)
    private UUID vendorId;

    @Column(name = "contracted_rate")
    private BigDecimal contractedRate;

    @Column(name = "negotiated_rate")
    private BigDecimal negotiatedRate;

    @Column(name = "vendor_budget")
    private BigDecimal vendorBudget;

    @Column(name = "allocated_hours")
    private Float allocatedHours;

    @Column(name = "calculated_hours")
    private Float calculatedHours;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private StatusEnum status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_term_id", referencedColumnName = "id")
    private ProgramTerm programTerm;
}
