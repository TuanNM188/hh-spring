package com.formos.huub.domain.entity;

import com.formos.huub.domain.entity.embedkey.LearningLibraryTagEmbedKey;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@Setter
@Entity
@Table(name = "Learning_library_tag")
public class LearningLibraryTag {

    @EmbeddedId
    private LearningLibraryTagEmbedKey id;
}
