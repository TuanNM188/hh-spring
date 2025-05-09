package com.formos.huub.domain.entity;

import com.formos.huub.domain.entity.embedkey.TechnicalAssistanceAdvisorEmbedKey;
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
@Table(name = "technical_assistance_advisor")
public class TechnicalAssistanceAdvisor {

    @EmbeddedId
    private TechnicalAssistanceAdvisorEmbedKey id;
}
