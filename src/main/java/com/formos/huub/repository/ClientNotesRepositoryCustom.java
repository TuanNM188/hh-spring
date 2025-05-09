package com.formos.huub.repository;

import com.formos.huub.domain.request.clientnote.RequestSearchClientNotes;
import com.formos.huub.domain.response.clientnote.ResponseSearchClientNote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ClientNotesRepositoryCustom {
    Page<ResponseSearchClientNote> searchClientNotesByBusinessOwnerId(
        UUID businessOwnerId,
        RequestSearchClientNotes request,
        Pageable pageable
    );
}
