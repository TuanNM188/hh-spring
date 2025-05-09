package com.formos.huub.repository;

import com.formos.huub.domain.entity.CommunityBoardUserRestriction;

import java.util.Optional;
import java.util.UUID;

import com.formos.huub.domain.enums.CommunityBoardRestrictionTypeEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CommunityBoardUserRestrictionRepository extends JpaRepository<CommunityBoardUserRestriction, UUID> {

    @Query("""
        SELECT EXISTS (
            select 1
            FROM CommunityBoardUserRestriction c
            WHERE c.userId = :userId and c.portalId = :portalId and c.isDelete = false and c.restrictionType = :type
        )
    """)
    Boolean existsByPortalIdAndUserId(UUID portalId, UUID userId, CommunityBoardRestrictionTypeEnum type);

    @Query("""
        SELECT c
        FROM CommunityBoardUserRestriction c
        WHERE c.userId = :userId and c.portalId = :portalId and c.isDelete = false and c.restrictionType = :type
    """)
    Optional<CommunityBoardUserRestriction> findByPortalIdAndUserId(UUID portalId, UUID userId, CommunityBoardRestrictionTypeEnum type);
}
