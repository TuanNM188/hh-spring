package com.formos.huub.repository;

import com.formos.huub.domain.entity.FundingSubmitted;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FundingSubmittedRepository extends JpaRepository<FundingSubmitted, UUID> {

    Optional<FundingSubmitted> findByUserIdAndFundingId(UUID userId, UUID fundingId);
}
