package com.formos.huub.domain.entity;

import com.formos.huub.domain.enums.FeatureCodeEnum;
import com.formos.huub.domain.enums.FeatureGroupCodeEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Collate;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

@Entity
@Table(name = "feature")
@Getter
@Setter
@SQLRestriction("is_delete='false'")
@SQLDelete(sql = "UPDATE feature SET is_delete = true WHERE id = ?")
public class Feature extends AbstractAuditingEntity<UUID> {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", length = 36)
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "feature_code", length = 50)
    private FeatureCodeEnum featureCode;

    @Column(name = "router_link")
    private String routerLink;

    @Column(name = "is_dynamic")
    private Boolean isDynamic;

    @Column(name = "priority_order")
    private Integer priorityOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "group_code", length = 50)
    private FeatureGroupCodeEnum groupCode;

    @Column(name = "group_name")
    private String groupName;

    @Column(name = "parent_id", length = 36)
    private UUID parentId;

    @Column(name = "feature_key")
    private String featureKey;

    @Column(name = "group_key")
    private String groupKey;
}
