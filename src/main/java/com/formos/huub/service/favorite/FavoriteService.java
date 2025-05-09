package com.formos.huub.service.favorite;

import com.formos.huub.domain.entity.UserFavorite;
import com.formos.huub.domain.enums.FavoriteTypeEnum;
import com.formos.huub.domain.enums.StatusEnum;
import com.formos.huub.repository.UserFavoriteRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FavoriteService {

    UserFavoriteRepository userFavoriteRepository;

    public void favorite(UUID userId, UUID entryId, FavoriteTypeEnum favoriteType, StatusEnum status) {
        var userFavorite = getUserFavorite(userId, entryId, favoriteType);
        userFavorite.setStatus(status);
        userFavoriteRepository.save(userFavorite);
    }

    private UserFavorite getUserFavorite(UUID userId, UUID entryId, FavoriteTypeEnum favoriteType) {
        return userFavoriteRepository.findByUserIdAndEntryIdAndFavoriteType(userId, entryId, favoriteType).orElse(
            UserFavorite.builder().userId(userId)
                .favoriteType(favoriteType)
                .entryId(entryId)
                .build()
        );
    }
}
