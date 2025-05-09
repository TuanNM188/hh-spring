package com.formos.huub.domain.response.project;

import com.formos.huub.domain.enums.EntityTypeEnum;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
public class ResponseProjectAttachment {

    private String id;

    private String name;

    private String realName;

    private String size;

    private String path;

    private String type;

    private String suffix;

    private String icon;

    private EntityTypeEnum entityType;

    private UUID entityId;


}
