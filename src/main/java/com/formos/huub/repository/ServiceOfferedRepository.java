package com.formos.huub.repository;

import com.formos.huub.domain.entity.ServiceOffered;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ServiceOfferedRepository extends JpaRepository<ServiceOffered, UUID> {


    @Query(value = "select s from ServiceOffered s where  s.category.id = :categoryId")
    List<ServiceOffered> getAllByCategoryId(UUID categoryId);

    @Query("select count(l) > 0 from ServiceOffered l where lower(l.name) = lower(:name) and l.category.id = :categoryId ")
    Boolean existsByNameAndCategoryId(String name, UUID categoryId);

    @Query("select count(l) > 0 from ServiceOffered l where lower(l.name) = lower(:name) and l.id != :id and l.category.id = :categoryId ")
    Boolean existsByNameAndNotEqualId(String name, UUID id, UUID categoryId);
}
