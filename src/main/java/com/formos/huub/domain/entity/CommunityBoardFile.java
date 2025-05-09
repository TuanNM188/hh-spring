package com.formos.huub.domain.entity;

import com.formos.huub.domain.enums.CommunityBoardEntryTypeEnum;
import com.formos.huub.domain.enums.CommunityBoardFileTypeEnum;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "community_board_file")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@SQLRestriction("is_delete='false'")
@SQLDelete(sql = "UPDATE community_board_file SET is_delete = true WHERE id = ?")
public class CommunityBoardFile extends AbstractAuditingEntity<UUID> {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", length = 36)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type")
    private CommunityBoardEntryTypeEnum entryType;

    @Column(name = "entry_id")
    private UUID entryId;

    @Column(name = "name")
    private String name;

    @Column(name = "real_name")
    private String realName;

    @Column(name = "path")
    private String path;

    @Column(name = "size")
    private String size;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private CommunityBoardFileTypeEnum type;

    @Column(name = "suffix")
    private String suffix;

    @Column(name = "index_order")
    private Integer indexOrder;

    @Column(name = "owner_id")
    private UUID ownerId;
}
