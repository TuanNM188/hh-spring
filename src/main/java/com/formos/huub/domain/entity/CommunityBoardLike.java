package com.formos.huub.domain.entity;

import com.formos.huub.domain.enums.CommunityBoardEntryTypeEnum;
import jakarta.persistence.*;
import java.util.UUID;

import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "community_board_like")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommunityBoardLike {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", length = 36)
    private UUID id;

    @Column(name = "author_id")
    private UUID authorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type")
    private CommunityBoardEntryTypeEnum entryType;

    @Column(name = "entry_id")
    private UUID entryId;

    @Column(name = "like_icon")
    private String likeIcon;
}
