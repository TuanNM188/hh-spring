package com.formos.huub.domain.response.technicaladvisor;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseBasicTechnicalAdvisor {

    private UUID id;

    private String name;

    private String imageUrl;
}
