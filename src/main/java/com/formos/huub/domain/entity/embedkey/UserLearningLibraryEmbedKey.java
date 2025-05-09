package com.formos.huub.domain.entity.embedkey;

import com.formos.huub.domain.entity.LearningLibrary;
import com.formos.huub.domain.entity.User;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class UserLearningLibraryEmbedKey {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "learning_library_id", referencedColumnName = "id")
    private LearningLibrary learningLibrary;
}
