package com.formos.huub.domain.entity;

import com.formos.huub.domain.entity.embedkey.PortalFeatureEmbedKey;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "portal_feature")
public class PortalFeature {

    @EmbeddedId
    private PortalFeatureEmbedKey id;

    @Column(name = "is_active", columnDefinition = "boolean default false", nullable = false)
    private Boolean isActive;
}
