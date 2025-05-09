package com.formos.huub.domain.response.member;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseMetaMember {

    private UUID userId;

    private String name;

    private String imageUrl;
}
