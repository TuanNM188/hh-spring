package com.formos.huub.repository;

import com.formos.huub.domain.entity.BookingSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingSettingRepository extends JpaRepository<BookingSetting, UUID> {

    Optional<BookingSetting> findByUserId(UUID userId);

    @Query("""
        SELECT bs
        FROM User u
        JOIN u.bookingSetting as bs
        JOIN u.technicalAdvisor as ta
        WHERE ta.id = :advisorId
    """)
    Optional<BookingSetting> findByTechnicalAdvisorId(UUID advisorId);

}
