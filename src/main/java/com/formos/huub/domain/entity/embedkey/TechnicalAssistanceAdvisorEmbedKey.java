package com.formos.huub.domain.entity.embedkey;

import com.formos.huub.domain.entity.TechnicalAdvisor;
import com.formos.huub.domain.entity.TechnicalAssistanceSubmit;
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
public class TechnicalAssistanceAdvisorEmbedKey {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technical_assistance_submit_id", referencedColumnName = "id")
    private TechnicalAssistanceSubmit technicalAssistanceSubmit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technical_advisor_id", referencedColumnName = "id")
    private TechnicalAdvisor technicalAdvisor;
}
