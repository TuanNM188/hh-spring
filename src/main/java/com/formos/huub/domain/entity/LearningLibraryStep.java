package com.formos.huub.domain.entity;

import com.formos.huub.tracker.TrackTranslate;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "learning_library_step")
@Getter
@Setter
@SQLRestriction("is_delete='false'")
@SQLDelete(sql = "UPDATE learning_library_step SET is_delete = true WHERE id = ?")
public class LearningLibraryStep extends AbstractAuditingEntity<UUID> {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", length = 36)
    private UUID id;

    @Column(name = "name")
    @TrackTranslate
    private String name;

    @Column(name = "priority_order")
    private Integer priorityOrder;

    @OneToMany(mappedBy = "learningLibraryStep")
    @OrderBy("priorityOrder ASC")
    private Set<LearningLibraryLesson> learningLibraryLessons = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "learning_library_id", referencedColumnName = "id")
    private LearningLibrary learningLibrary;
}
