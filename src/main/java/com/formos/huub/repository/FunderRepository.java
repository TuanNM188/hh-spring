package com.formos.huub.repository;

import com.formos.huub.domain.entity.Funder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FunderRepository extends JpaRepository<Funder, UUID> {

    @Query("SELECT count(f) > 0 FROM Funder f WHERE lower(f.name) = lower(:name)")
    Boolean existsByName(String name);

    @Query("SELECT count(f) > 0 FROM Funder f WHERE lower(f.name) = lower(:name) AND f.id != :id")
    Boolean existsByNameAndNotEqualId(String name, UUID id);

}
