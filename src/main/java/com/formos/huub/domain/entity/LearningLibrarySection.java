package com.formos.huub.domain.entity;

import com.formos.huub.config.GsonConfiguration;
import com.formos.huub.domain.enums.SectionTypeEnum;
import com.formos.huub.tracker.TrackTranslate;
import com.google.gson.JsonElement;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "learning_library_section")
@Getter
@Setter
@SQLRestriction("is_delete='false'")
@SQLDelete(sql = "UPDATE learning_library_section SET is_delete = true WHERE id = ?")
public class LearningLibrarySection extends AbstractAuditingEntity<UUID> {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", length = 36)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "section_type", length = 50)
    private SectionTypeEnum sectionType;

    @Column(name = "position")
    private Integer position;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "contents", columnDefinition = "json")
    @Convert(converter = GsonConfiguration.JsonElementConverter.class)
    @TrackTranslate
    private JsonElement contents;

    @Column(name = "survey_title")
    private String surveyTitle;

    @Column(name = "survey_description")
    private String surveyDescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "learning_library_lesson_id", referencedColumnName = "id")
    private LearningLibraryLesson learningLibraryLesson;
}
