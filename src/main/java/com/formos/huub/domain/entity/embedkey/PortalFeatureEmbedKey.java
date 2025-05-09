package com.formos.huub.domain.entity.embedkey;

import com.formos.huub.domain.entity.Feature;
import com.formos.huub.domain.entity.Portal;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class PortalFeatureEmbedKey {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portal_id", referencedColumnName = "id")
    private Portal portal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feature_id", referencedColumnName = "id")
    private Feature feature;
}
