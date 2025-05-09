package com.formos.huub.repository;

import com.formos.huub.domain.entity.SpecialtyArea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpecialtyAreaRepository extends JpaRepository<SpecialtyArea, UUID> {

    List<SpecialtyArea> findAllBySpecialtyId(UUID specialtyId);

    @Query("SELECT count(s) > 0 FROM SpecialtyArea s WHERE lower(s.name) = lower(:name) AND s.specialty.id = :specialtyId")
    Boolean existByNameAndSpecialtyId(String name, UUID specialtyId);

    @Query("SELECT count(s) > 0 FROM SpecialtyArea s WHERE lower(s.name) = lower(:name) AND s.id != :id AND s.specialty.id = :specialtyId")
    Boolean existByNameAndNotEqualId(String name, UUID id, UUID specialtyId);


}
