package com.formos.huub.repository;

import com.formos.huub.domain.entity.Translate;
import com.formos.huub.framework.enums.LanguageEnum;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TranslateRepository extends JpaRepository<Translate, UUID> {
    void deleteAllBySourceHash(String sourceHash);
    List<Translate> findBySourceLanguageAndTargetLanguageAndSourceHash(
        LanguageEnum sourceLanguage,
        LanguageEnum targetLanguage,
        String sourceHash
    );
}
