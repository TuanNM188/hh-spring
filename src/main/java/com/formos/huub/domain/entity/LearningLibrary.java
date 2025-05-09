package com.formos.huub.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.formos.huub.domain.enums.AccessTypeEnum;
import com.formos.huub.domain.enums.ContentTypeEnum;
import com.formos.huub.domain.enums.LearningLibraryStatusEnum;
import com.formos.huub.domain.enums.LearningStatusEnum;
import com.formos.huub.domain.response.learninglibrary.ResponseSearchLearningLibrary;
import com.formos.huub.domain.response.learninglibrary.ResponseSearchLearningLibraryCardView;
import com.formos.huub.tracker.TrackTranslate;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "learning_library")
@Getter
@Setter
@SQLRestriction("is_delete='false'")
@SQLDelete(sql = "UPDATE learning_library SET is_delete = true WHERE id = ?")
@SqlResultSetMappings(
    {
        @SqlResultSetMapping(
            name = "search_learning_library",
            classes = @ConstructorResult(
                targetClass = ResponseSearchLearningLibrary.class,
                columns = {
                    @ColumnResult(name = "id", type = UUID.class),
                    @ColumnResult(name = "name", type = String.class),
                    @ColumnResult(name = "contentType", type = ContentTypeEnum.class),
                    @ColumnResult(name = "accessType", type = AccessTypeEnum.class),
                    @ColumnResult(name = "numSteps", type = Integer.class),
                    @ColumnResult(name = "status", type = LearningLibraryStatusEnum.class),
                    @ColumnResult(name = "userCreatedId", type = UUID.class),
                }
            )
        ),
        @SqlResultSetMapping(
            name = "search_learning_library_card_view",
            classes = @ConstructorResult(
                targetClass = ResponseSearchLearningLibraryCardView.class,
                columns = {
                    @ColumnResult(name = "id", type = UUID.class),
                    @ColumnResult(name = "name", type = String.class),
                    @ColumnResult(name = "description", type = String.class),
                    @ColumnResult(name = "thumbnail", type = String.class),
                    @ColumnResult(name = "lessons", type = Integer.class),
                    @ColumnResult(name = "completionStatus", type = LearningStatusEnum.class),
                    @ColumnResult(name = "isBookmark", type = Boolean.class),
                    @ColumnResult(name = "categoryId", type = UUID.class),
                    @ColumnResult(name = "categoryName", type = String.class),
                    @ColumnResult(name = "categoryIcon", type = String.class),
                    @ColumnResult(name = "rating", type = Integer.class),
                    @ColumnResult(name = "userCreatedId", type = UUID.class),
                }
            )
        ),
    }
)
public class LearningLibrary extends AbstractAuditingEntity<UUID> {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", length = 36)
    private UUID id;

    @Column(name = "name")
    @TrackTranslate
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", length = 50)
    private ContentTypeEnum contentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "access_type", length = 50)
    private AccessTypeEnum accessType;

    @Column(name = "expires_in")
    private Integer expiresIn;

    @Column(name = "enrollment_deadline")
    private Instant enrollmentDeadline;

    @Column(name = "enrollee_limit")
    private Integer enrolleeLimit;

    @Column(name = "start_date")
    private Instant startDate;

    @Column(name = "end_date")
    private Instant endDate;

    @Column(name = "price")
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private LearningLibraryStatusEnum status;

    @Column(name = "published_date")
    private Instant publishedDate;

    @Column(name = "description")
    @TrackTranslate
    private String description;

    @Column(name = "hero_image")
    private String heroImage;

    @Column(name = "user_created_id")
    private UUID userCreatedId;

    @Column(name = "is_registration_form_required", columnDefinition = "boolean default false")
    private Boolean isRegistrationFormRequired;

    @Column(name = "survey_json")
    private String surveyJson;

    @Column(name = "number_of_registered")
    private Integer numberOfRegistered;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", referencedColumnName = "id")
    private Category category;

    @OneToMany(mappedBy = "learningLibrary")
    @OrderBy("priorityOrder ASC")
    private Set<LearningLibraryStep> learningLibrarySteps = new HashSet<>();

    @JsonIgnore
    @ManyToMany
    @JoinTable(
        name = "learning_library_tag",
        joinColumns = { @JoinColumn(name = "learning_library_id", referencedColumnName = "id") },
        inverseJoinColumns = { @JoinColumn(name = "tag_id", referencedColumnName = "id") }
    )
    private Set<Tag> tags = new HashSet<>();

    @JsonIgnore
    @ManyToMany
    @JoinTable(
        name = "learning_library_speaker",
        joinColumns = { @JoinColumn(name = "learning_library_id", referencedColumnName = "id") },
        inverseJoinColumns = { @JoinColumn(name = "speaker_id", referencedColumnName = "id") }
    )
    private Set<Speaker> speakers = new HashSet<>();

    @JsonIgnore
    @ManyToMany
    @JoinTable(
        name = "learning_library_portal",
        joinColumns = { @JoinColumn(name = "learning_library_id", referencedColumnName = "id") },
        inverseJoinColumns = { @JoinColumn(name = "portal_id", referencedColumnName = "id") }
    )
    private Set<Portal> portals = new HashSet<>();

    @OneToMany(mappedBy = "learningLibrary", fetch = FetchType.LAZY)
    private Set<TermsAcceptance> termsAcceptances = new HashSet<>();

    @OneToMany(mappedBy = "learningLibrary", fetch = FetchType.LAZY)
    private Set<LearningLibraryRegistration> learningLibraryRegistrations = new HashSet<>();
}
