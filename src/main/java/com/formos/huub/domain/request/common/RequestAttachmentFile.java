package com.formos.huub.domain.request.common;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RequestAttachmentFile {

    private String id;

    private String url;

    private String name;

    private String realName;

    private String size;

    private String contentType;

    private String icon;

    private String entityType;
}
