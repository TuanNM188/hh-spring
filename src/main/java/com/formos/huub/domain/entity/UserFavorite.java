package com.formos.huub.domain.entity;

import com.formos.huub.domain.enums.FavoriteTypeEnum;
import com.formos.huub.domain.enums.StatusEnum;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

@Entity
@Table(name = "user_favorite")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@SQLRestriction("is_delete='false'")
@SQLDelete(sql = "UPDATE user_favorite SET is_delete = true WHERE id = ?")
public class UserFavorite extends AbstractAuditingEntity<UUID> {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", length = 36)
    private UUID id;

    @Column(name = "user_id", length = 36)
    private UUID userId;

    @Column(name = "entry_id", length = 36)
    private UUID entryId;

    @Enumerated(EnumType.STRING)
    @Column(name = "favorite_type", length = 50)
    private FavoriteTypeEnum favoriteType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private StatusEnum status;

}
