package com.formos.huub.repository;

import com.formos.huub.domain.entity.PortalFunding;
import com.formos.huub.domain.entity.embedkey.PortalFundingEmbedKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PortalFundingRepository extends JpaRepository<PortalFunding, PortalFundingEmbedKey> {
}
