package com.formos.huub.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "portal_intake_question")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@SQLRestriction("is_delete='false'")
@SQLDelete(sql = "UPDATE portal_intake_question SET is_delete = true WHERE id = ?")
public class PortalIntakeQuestion extends AbstractAuditingEntity<UUID> {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", length = 36)
    private UUID id;

    @Column(name = "portal_id")
    private UUID portalId;

    @Column(name = "question_id", length = 36)
    private UUID questionId;

    @Column(name = "is_visible")
    private Boolean isVisible;

    @Column(name = "priority_order")
    private Integer priorityOrder;

    @Column(name = "column_size")
    private String columnSize;

    @Column(name = "allow_other_input")
    private Boolean allowOtherInput;

}
