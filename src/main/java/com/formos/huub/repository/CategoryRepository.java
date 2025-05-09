package com.formos.huub.repository;

import com.formos.huub.domain.entity.Category;
import com.formos.huub.domain.response.categories.IResponseCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    @Query("select count(l) > 0 from Category l where lower(l.name) = lower(:name) ")
    Boolean existsByName(String name);

    @Query("select count(l) > 0 from Category l where lower(l.name) = lower(:name) and l.id != :id")
    Boolean existsByNameAndNotEqualId(String name, UUID id);

    @Query(value = "SELECT c.id AS id, c.name AS name, " +
        "json_agg(CASE WHEN so.id IS NOT NULL THEN json_build_object('id', so.id, 'name', so.name) ELSE 'null' END) AS serviceOptions " +
        "FROM Category c " +
        "LEFT JOIN ServiceOffered so ON c.id = so.category.id AND c.isDelete IS false AND so.isDelete IS false " +
        "GROUP BY c.id, c.name")
    List<IResponseCategory> getAllCategoriesAndServices();

}
