package com.formos.huub.domain.entity;

import com.formos.huub.domain.enums.EntityTypeEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

@Entity
@Table(name = "entity_attachment")
@Getter
@Setter
@SQLRestriction("is_delete='false'")
@SQLDelete(sql = "UPDATE entity_attachment SET is_delete = true WHERE id = ?")
public class EntityAttachment extends AbstractAuditingEntity<UUID> {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", length = 36)
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "real_name")
    private String realName;

    @Column(name = "path")
    private String path;

    @Column(name = "type")
    private String type;

    @Column(name = "suffix")
    private String suffix;

    @Column(name = "icon")
    private String icon;

    @Column(name = "size")
    private String size;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type")
    private EntityTypeEnum entityType;

    @Column(name = "entity_id")
    private UUID entityId;
}
