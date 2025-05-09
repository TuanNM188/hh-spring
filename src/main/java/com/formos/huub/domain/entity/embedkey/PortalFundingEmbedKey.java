package com.formos.huub.domain.entity.embedkey;

import com.formos.huub.domain.entity.Funding;
import com.formos.huub.domain.entity.Portal;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;

@Embeddable
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PortalFundingEmbedKey {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portal_id", referencedColumnName = "id")
    private Portal portal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "funding_id", referencedColumnName = "id")
    private Funding funding;
}
