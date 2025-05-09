package com.formos.huub.domain.entity;

import com.formos.huub.domain.enums.AccessTypeEnum;
import com.formos.huub.domain.enums.RegistrationStatusEnum;
import com.formos.huub.domain.response.businessowner.ResponseSearchCourseRegistrations;
import com.formos.huub.domain.response.learninglibraryregistration.ResponseSearchLearningLibraryRegistration;
import com.formos.huub.domain.response.learninglibraryregistration.ResponseSearchLessonSurvey;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "learning_library_registration")
@Getter
@Setter
@SQLRestriction("is_delete='false'")
@SQLDelete(sql = "UPDATE learning_library_registration SET is_delete = true WHERE id = ?")
@SqlResultSetMappings(
    {
        @SqlResultSetMapping(
            name = "search_course_registrations",
            classes = @ConstructorResult(
                targetClass = ResponseSearchCourseRegistrations.class,
                columns = {
                    @ColumnResult(name = "id", type = UUID.class),
                    @ColumnResult(name = "name", type = String.class),
                    @ColumnResult(name = "accessType", type = String.class),
                    @ColumnResult(name = "categoryId", type = UUID.class),
                    @ColumnResult(name = "categoryName", type = String.class),
                    @ColumnResult(name = "enrolledDate", type = Instant.class),
                    @ColumnResult(name = "startedDate", type = Instant.class),
                    @ColumnResult(name = "completedDate", type = Instant.class),
                    @ColumnResult(name = "rating", type = Integer.class),
                    @ColumnResult(name = "completedLesson", type = Integer.class),
                    @ColumnResult(name = "totalLesson", type = Integer.class),
                }
            )
        ),
        @SqlResultSetMapping(
            name = "search_course_surveys",
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
                }
            )
        ),
        @SqlResultSetMapping(
            name = "search_learning_library_registrations",
            classes = @ConstructorResult(
                targetClass = ResponseSearchLearningLibraryRegistration.class,
                columns = {
                    @ColumnResult(name = "id", type = UUID.class),
                    @ColumnResult(name = "businessOwnerName", type = String.class),
                    @ColumnResult(name = "courseName", type = String.class),
                    @ColumnResult(name = "submissionDate", type = Instant.class),
                    @ColumnResult(name = "status", type = RegistrationStatusEnum.class),
                    @ColumnResult(name = "portalName", type = String.class),
                }
            )
        ),
    }
)
public class LearningLibraryRegistration extends AbstractAuditingEntity<UUID> {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", length = 36)
    private UUID id;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "registration_status", length = 50)
    private RegistrationStatusEnum registrationStatus;

    @Column(name = "registration_date")
    private Instant registrationDate;

    @Column(name = "survey_data")
    private String surveyData;

    @Column(name = "pdf_url")
    private String pdfUrl;

    @Column(name = "last_activity_date")
    private Instant lastActivityDate;

    @Column(name = "decision_date")
    private Instant decisionDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "course_type", length = 50)
    private AccessTypeEnum courseType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "learning_library_id", referencedColumnName = "id")
    private LearningLibrary learningLibrary;

    @OneToMany(mappedBy = "learningLibraryRegistration", fetch = FetchType.LAZY)
    private Set<LearningLibraryRegistrationDetail> learningLibraryRegistrationDetails = new HashSet<>();
}
