package com.formos.huub.repository;

import com.formos.huub.domain.entity.PortalActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.UUID;

@Repository
public interface PortalActivityLogRepository extends JpaRepository<PortalActivityLog, UUID> {

    @Modifying
    @Query("DELETE FROM PortalActivityLog p WHERE p.createdDate < :day")
    void deleteAllByDate(Instant day);
}
