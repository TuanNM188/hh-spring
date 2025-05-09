package com.formos.huub.domain.entity;

import com.formos.huub.domain.enums.*;
import com.formos.huub.tracker.TrackTranslate;
import jakarta.persistence.*;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "question")
@Getter
@Setter
@SQLRestriction("is_delete='false'")
@SQLDelete(sql = "UPDATE question SET is_delete = true WHERE id = ?")
public class Question extends AbstractAuditingEntity<UUID> {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", length = 36)
    private UUID id;

    @Column(name = "question", length = 1000)
    @TrackTranslate
    private String question;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", length = 50)
    private QuestionTypeEnum questionType;

    @Column(name = "question_code")
    private String questionCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "group_code", length = 50)
    private GroupCodeEnum groupCode;

    @Column(name = "priority_order")
    private Integer priorityOrder;

    @Column(name = "is_require")
    private Boolean isRequire;

    @Enumerated(EnumType.STRING)
    @Column(name = "form_code", length = 50)
    private FormCodeEnum formCode;

    @Column(name = "parent_id", length = 36)
    private UUID parentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "option_type", length = 50)
    private OptionTypeEnum optionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "input_type", length = 50)
    private InputTypeEnum inputType;

    @OneToMany(mappedBy = "question", fetch = FetchType.LAZY)
    private Set<AnswerOption> answerOptions;

    @Column(name = "allow_custom_options", columnDefinition = "boolean default false")
    private Boolean allowCustomOptions;

    @Column(name = "column_size")
    private String columnSize;

    @Column(name = "placeholder")
    @TrackTranslate
    private String placeholder;

    @Column(name = "allow_action_visible", columnDefinition = "boolean default false")
    private Boolean allowActionVisible;

    @Column(name = "description", length = 1000)
    @TrackTranslate
    private String description;

    @Column(name = "message_validate", length = 1000)
    @TrackTranslate
    private String messageValidate;

    @Column(name = "display_form")
    private String displayForm;

    @Column(name = "column_size_for_member_form")
    private String columnSizeForMemberForm;

    @Column(name = "priority_order_for_member_form")
    private Integer priorityOrderForMemberForm;

    @Enumerated(EnumType.STRING)
    @Column(name = "group_code_for_member")
    private GroupCodeEnum groupCodeForMember;

    @Column(name = "column_size_for_business_owner_form")
    private String columnSizeForBusinessOwnerForm;

    @Column(name = "priority_order_for_business_owner_form")
    private Integer priorityOrderForBusinessOwnerForm;
}
