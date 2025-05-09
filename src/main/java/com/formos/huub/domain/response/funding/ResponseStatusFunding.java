package com.formos.huub.domain.response.funding;

import com.formos.huub.domain.enums.StatusEnum;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseStatusFunding {

    private UUID id;

    private StatusEnum status;
}
