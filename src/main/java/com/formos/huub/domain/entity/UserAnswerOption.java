package com.formos.huub.domain.entity;

import com.formos.huub.domain.enums.OptionTypeEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

@Entity
@Table(name = "user_answer_option")
@Getter
@Setter
@SQLRestriction("is_delete='false'")
@SQLDelete(sql = "UPDATE user_answer_option SET is_delete = true WHERE id = ?")
public class UserAnswerOption  extends AbstractAuditingEntity<UUID> {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", length = 36)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    private UserAnswerForm userAnswerForm;

    @Column(name = "option_id")
    private UUID optionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "option_type", length = 50)
    private OptionTypeEnum optionType;

    @Column(name = "priority_order")
    private Integer priorityOrder;

}
