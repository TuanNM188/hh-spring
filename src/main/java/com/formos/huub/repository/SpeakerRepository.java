package com.formos.huub.repository;

import com.formos.huub.domain.entity.Speaker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpeakerRepository extends JpaRepository<Speaker, UUID> {

    @Query("SELECT s " +
        "FROM Speaker s " +
        "JOIN s.learningLibraries l " +
        "WHERE l.id = :learningLibraryId")
    List<Speaker> getSpeakerByLearningLibraryId(UUID learningLibraryId);

}
