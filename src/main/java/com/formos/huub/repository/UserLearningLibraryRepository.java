package com.formos.huub.repository;

import com.formos.huub.domain.entity.UserLearningLibrary;
import com.formos.huub.domain.entity.embedkey.UserLearningLibraryEmbedKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserLearningLibraryRepository extends JpaRepository<UserLearningLibrary, UserLearningLibraryEmbedKey> {

    Optional<UserLearningLibrary> findById_User_IdAndId_LearningLibrary_Id(UUID userId, UUID learningLibraryId);

}
