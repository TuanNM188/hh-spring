package com.formos.huub.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.formos.huub.domain.enums.MenuPositionEnum;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "menu")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("is_delete='false'")
@SQLDelete(sql = "UPDATE menu SET is_delete = true WHERE id = ?")
public class Menu extends AbstractAuditingEntity<UUID> {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", length = 36)
    private UUID id;

    @Column(name = "label")
    private String label;

    @Column(name = "icon")
    private String icon;

    @Column(name = "router_link")
    private String routerLink;

    @Column(name = "priority_order")
    private Integer priorityOrder;

    @Column(name = "is_active", columnDefinition = "boolean default true")
    private Boolean isActive;

    @Column(name = "position")
    @Enumerated(EnumType.STRING)
    private MenuPositionEnum position;

    @Column(name = "is_public", columnDefinition = "boolean default false")
    private Boolean isPublic;

    @Column(name = "menu_code")
    private String menuCode;

    @Column(name = "menuKey")
    private String menuKey;

    @JsonIgnore
    @ManyToMany
    @JoinTable(
        name = "menu_authority",
        joinColumns = { @JoinColumn(name = "menu_id", referencedColumnName = "id") },
        inverseJoinColumns = { @JoinColumn(name = "authority_name", referencedColumnName = "name") }
    )
    @BatchSize(size = 20)
    private Set<Authority> authorities;

}
