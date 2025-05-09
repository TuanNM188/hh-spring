package com.formos.huub.repository;

import com.formos.huub.domain.entity.ServiceOutcome;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ServiceOutcomeRepository extends JpaRepository<ServiceOutcome, UUID> {


    @Query(value = "SELECT s FROM ServiceOutcome s WHERE  s.serviceOffered.id = :serviceOfferedId")
    List<ServiceOutcome> getAllByServiceOfferedId(UUID serviceOfferedId);

    @Query("SELECT count(s) > 0 FROM ServiceOutcome s WHERE lower(s.name) = lower(:name) AND s.serviceOffered.id = :serviceOfferedId")
    Boolean existByNameAndServiceOfferedId(String name, UUID serviceOfferedId);

    @Query("SELECT count(s) > 0 FROM ServiceOutcome s WHERE lower(s.name) = lower(:name) AND s.id != :id AND s.serviceOffered.id = :serviceOfferedId")
    Boolean existByNameAndNotEqualId(String name, UUID id, UUID serviceOfferedId);
}
