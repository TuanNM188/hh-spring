package com.formos.huub.repository;

import com.formos.huub.domain.entity.ClientNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ClientNoteRepository extends JpaRepository<ClientNote, UUID>, ClientNotesRepositoryCustom {
}
