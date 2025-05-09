package com.formos.huub.domain.entity;

import com.formos.huub.domain.enums.EntryTypeEnum;
import com.formos.huub.domain.enums.FormCodeEnum;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "user_answer_form")
@Getter
@Setter
@SQLRestriction("is_delete='false'")
@SQLDelete(sql = "UPDATE user_answer_form SET is_delete = true WHERE id = ?")
public class UserAnswerForm extends AbstractAuditingEntity<UUID> {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", length = 36)
    private UUID id;

    @Column(name = "entry_id")
    private UUID entryId;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", length = 50)
    private EntryTypeEnum entryType;

    @Column(name = "question_id", length = 36)
    private UUID questionId;

    @Column(name = "additional_answer")
    private String additionalAnswer  ;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "entry_form_id", length = 36)
    private UUID entryFormId;

    @OneToMany(mappedBy = "userAnswerForm", fetch = FetchType.LAZY)
    private Set<UserAnswerOption> userAnswerOptions;
}
