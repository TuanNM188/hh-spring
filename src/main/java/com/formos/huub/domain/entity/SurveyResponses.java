package com.formos.huub.domain.entity;

import com.formos.huub.domain.response.survey.ResponseSearchSurveyResponses;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "survey_responses")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("is_delete='false'")
@SQLDelete(sql = "UPDATE survey_responses SET is_delete = true WHERE id = ?")
@SqlResultSetMappings(
    {
        @SqlResultSetMapping(
            name = "search_survey_responses",
            classes = @ConstructorResult(
                targetClass = ResponseSearchSurveyResponses.class,
                columns = {
                    @ColumnResult(name = "id", type = UUID.class),
                    @ColumnResult(name = "submissionDate", type = Instant.class),
                    @ColumnResult(name = "fullName", type = String.class),
                    @ColumnResult(name = "role", type = String.class),
                    @ColumnResult(name = "pdfUrl", type = String.class),
                    @ColumnResult(name = "surveyName", type = String.class),
                }
            )
        ),
    }
)
public class SurveyResponses extends AbstractAuditingEntity<UUID> {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", length = 36)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Column(name = "submission_date")
    private Instant submissionDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id", referencedColumnName = "id")
    private Survey survey;

    @Column(name = "survey_data")
    private String surveyData;

    @Column(name = "pdf_url")
    private String pdfUrl;
}
