package com.formos.huub.domain.entity;

import com.formos.huub.domain.enums.LearningStatusEnum;
import com.formos.huub.domain.enums.RegistrationStatusEnum;
import com.formos.huub.domain.response.businessowner.ResponseSearchCourseRegistrations;
import com.formos.huub.domain.response.learninglibraryregistration.ResponseSearchLearningLibraryRegistration;
import com.formos.huub.domain.response.learninglibraryregistration.ResponseSearchLessonSurvey;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "learning_library_registration_detail")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@SQLRestriction("is_delete='false'")
@SQLDelete(sql = "UPDATE learning_library_registration_detail SET is_delete = true WHERE id = ?")
@SqlResultSetMappings(
    {
        @SqlResultSetMapping(
            name = "search_lesson_surveys",
            classes = @ConstructorResult(
                targetClass = ResponseSearchLessonSurvey.class,
                columns = {
                    @ColumnResult(name = "id", type = UUID.class),
                    @ColumnResult(name = "businessOwnerName", type = String.class),
                    @ColumnResult(name = "courseName", type = String.class),
                    @ColumnResult(name = "lessonName", type = String.class),
                    @ColumnResult(name = "submissionDate", type = Instant.class),
                    @ColumnResult(name = "surveyData", type = String.class),
                    @ColumnResult(name = "surveyName", type = String.class),
                    @ColumnResult(name = "surveyDescription", type = String.class),
                    @ColumnResult(name = "portalName", type = String.class),
                }
            )
        ),
    }
)
public class LearningLibraryRegistrationDetail extends AbstractAuditingEntity<UUID> {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", length = 36)
    private UUID id;

    @Column(name = "survey_data")
    private String surveyData;

    @Column(name = "pdf_url")
    private String pdfUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "learning_status", length = 50)
    private LearningStatusEnum learningStatus;

    @Column(name = "lesson_id")
    private UUID lessonId;

    @Column(name = "submission_date")
    private Instant submissionDate;

    @Column(name = "completion_date")
    private Instant completionDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "learning_library_registration_id", referencedColumnName = "id")
    private LearningLibraryRegistration learningLibraryRegistration;

}
