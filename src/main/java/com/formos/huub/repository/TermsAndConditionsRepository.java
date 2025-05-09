package com.formos.huub.repository;

import com.formos.huub.domain.entity.TermsAndConditions;
import com.formos.huub.domain.enums.TermsAndConditionsTypeEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TermsAndConditionsRepository extends JpaRepository<TermsAndConditions, UUID> {

    @Query(value = "SELECT t FROM TermsAndConditions t WHERE t.isActive IS true AND t.type = :type")
    Optional<TermsAndConditions> getTermsAndConditionsByType(TermsAndConditionsTypeEnum type);

}
