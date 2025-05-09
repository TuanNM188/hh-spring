package com.formos.huub.domain.entity;

import com.formos.huub.domain.response.survey.ResponseSearchSurvey;
import jakarta.persistence.*;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "survey")
@Getter
@Setter
@SQLRestriction("is_delete='false'")
@SQLDelete(sql = "UPDATE survey SET is_delete = true WHERE id = ?")
@SqlResultSetMappings(
    {
        @SqlResultSetMapping(
            name = "search_survey",
            classes = @ConstructorResult(
                targetClass = ResponseSearchSurvey.class,
                columns = {
                    @ColumnResult(name = "id", type = UUID.class),
                    @ColumnResult(name = "name", type = String.class),
                    @ColumnResult(name = "description", type = String.class),
                    @ColumnResult(name = "isActive", type = Boolean.class),
                    @ColumnResult(name = "portalName", type = String.class),
                    @ColumnResult(name = "responses", type = Integer.class),
                    @ColumnResult(name = "portalUrl", type = String.class),
                    @ColumnResult(name = "isCustomDomain", type = Boolean.class),
                }
            )
        ),
    }
)
public class Survey extends AbstractAuditingEntity<UUID> {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", length = 36)
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portal_id", referencedColumnName = "id")
    private Portal portal;

    @Column(name = "is_active", columnDefinition = "boolean default true")
    private Boolean isActive;

    @Column(name = "survey_json")
    private String surveyJson;

    @OneToMany(mappedBy = "survey")
    @OrderBy("submissionDate DESC")
    private Set<SurveyResponses> responses;
}
