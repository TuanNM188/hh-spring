package com.formos.huub.repository;

import com.formos.huub.domain.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TagRepository extends JpaRepository<Tag, UUID> {

    List<Tag> findAlByIdIn(List<UUID> ids);

    List<Tag> findByNameIn(List<String> names);
}
