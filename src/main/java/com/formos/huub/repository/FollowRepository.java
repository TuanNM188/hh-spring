package com.formos.huub.repository;

import com.formos.huub.domain.entity.Follow;
import com.formos.huub.domain.enums.FollowStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FollowRepository extends JpaRepository<Follow, UUID> {

    Optional<Follow> findByFollowedIdAndFollowerId(UUID followedId, UUID followerId);

    boolean existsByFollowedIdAndFollowerIdAndStatus(UUID followedId, UUID followerId, FollowStatusEnum status);

    @Query("""
        select f.follower.id
        from Follow f
        where f.followed.id  = :followedId
            and (:ignoredFollowerIds is null or f.follower.id not in :ignoredFollowerIds)
    """)
    List<UUID> findAllByFollowedId(UUID followedId, List<UUID> ignoredFollowerIds);
}
