package com.formos.huub.domain.request.projectreport;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RequestPRAttachmentFile {

    private String id;

    private String url;

    private String name;

    private String realName;

    private String size;

    private String icon;

    private String contentType;
}
