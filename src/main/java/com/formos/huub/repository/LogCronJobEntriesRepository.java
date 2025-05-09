package com.formos.huub.repository;

import com.formos.huub.domain.entity.LogCronJobEntries;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Repository
public interface LogCronJobEntriesRepository extends JpaRepository<LogCronJobEntries, UUID> {

    @Transactional
    @Modifying
    @Query("DELETE FROM LogCronJobEntries l WHERE l.timestamp < :date")
    void deleteDataOlderThan(Instant date);

}
