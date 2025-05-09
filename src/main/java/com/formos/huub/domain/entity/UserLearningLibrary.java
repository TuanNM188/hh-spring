package com.formos.huub.domain.entity;

import com.formos.huub.domain.entity.embedkey.UserLearningLibraryEmbedKey;
import com.formos.huub.domain.enums.LearningStatusEnum;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "user_learning_library")
public class UserLearningLibrary {

    @EmbeddedId
    private UserLearningLibraryEmbedKey id;

    @Column(name = "completion_status")
    @Enumerated(EnumType.STRING)
    private LearningStatusEnum completionStatus;

    @Column(name = "is_bookmark", columnDefinition = "boolean default false")
    private Boolean isBookmark ;

    @Column(name = "rating")
    private Integer rating;
}
