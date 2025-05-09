package com.formos.huub.repository;

import com.formos.huub.domain.entity.UserFavorite;
import com.formos.huub.domain.enums.FavoriteTypeEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserFavoriteRepository extends JpaRepository<UserFavorite, UUID> {

    Optional<UserFavorite> findByUserIdAndEntryIdAndFavoriteType(UUID userId, UUID entryId, FavoriteTypeEnum favoriteType);
}
