package com.formos.huub.mapper.usersociallink;

import com.formos.huub.domain.entity.UserSocialLink;
import com.formos.huub.domain.response.account.ResponseUserSocialLink;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserSocialLinkMapper {

    List<ResponseUserSocialLink> toListResponse(List<UserSocialLink> userSocialLinks);

}
