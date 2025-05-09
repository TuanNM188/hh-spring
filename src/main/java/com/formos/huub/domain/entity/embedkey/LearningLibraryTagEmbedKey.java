package com.formos.huub.domain.entity.embedkey;

import com.formos.huub.domain.entity.LearningLibrary;
import com.formos.huub.domain.entity.Tag;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;

@Embeddable
@Getter
@Setter
public class LearningLibraryTagEmbedKey {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", referencedColumnName = "id")
    private Tag tag;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "learning_library_id", referencedColumnName = "id")
    private LearningLibrary learningLibrary;
}
