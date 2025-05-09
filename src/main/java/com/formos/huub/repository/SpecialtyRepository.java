package com.formos.huub.repository;

import com.formos.huub.domain.entity.Specialty;
import com.formos.huub.domain.response.specialty.IResponseSpecialtyAndArea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpecialtyRepository extends JpaRepository<Specialty, UUID> {


    @Query("select count(l) > 0 from Specialty l where lower(l.name) = lower(:name)")
    Boolean existsByName(String name);

    @Query("select count(l) > 0 from Specialty l where lower(l.name) = lower(:name) and l.id != :specialtyId")
    Boolean existsByNameNotEqualId(String name, UUID specialtyId);

    @Query("select s from Specialty s order by s.createdDate desc ")
    List<Specialty> findAll();


    @Query("SELECT s.id AS id, s.name AS name, " +
        "json_agg(CASE WHEN sa.id IS NOT NULL THEN json_build_object('id', sa.id, 'name', sa.name) ELSE 'null' END) AS specialtyAreas " +
        "FROM Specialty s " +
        "LEFT JOIN SpecialtyArea sa ON s.id = sa.specialty.id  AND sa.isDelete IS false " +
        "GROUP BY s.id, s.name order by s.createdDate desc ")
    List<IResponseSpecialtyAndArea> getAllSpecialtyAndArea();

}
