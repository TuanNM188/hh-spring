package com.formos.huub.repository;

import com.formos.huub.domain.entity.LearningLibraryTag;
import com.formos.huub.domain.entity.embedkey.LearningLibraryTagEmbedKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LearningLibraryTagRepository extends JpaRepository<LearningLibraryTag, LearningLibraryTagEmbedKey> {

    void deleteById_LearningLibraryId(UUID learningLibraryId);
}
