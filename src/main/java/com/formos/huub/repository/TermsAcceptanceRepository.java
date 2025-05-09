package com.formos.huub.repository;

import com.formos.huub.domain.entity.TermsAcceptance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface  TermsAcceptanceRepository extends JpaRepository<TermsAcceptance, UUID> {

}
