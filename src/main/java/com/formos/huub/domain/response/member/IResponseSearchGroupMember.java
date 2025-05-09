package com.formos.huub.domain.response.member;

import java.util.UUID;

public interface IResponseSearchGroupMember extends IResponseSearchMember {
    String getGroupRole();
    UUID getGroupId();
}
