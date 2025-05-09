package com.formos.huub.domain.entity;

import com.formos.huub.domain.entity.embedkey.PortalFundingEmbedKey;
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
@Table(name = "funding_portal")
public class PortalFunding {

    @EmbeddedId
    private PortalFundingEmbedKey id;
}
