package com.formos.huub.mapper.member;

import com.formos.huub.domain.entity.User;
import com.formos.huub.domain.request.member.RequestMemberProfile;
import com.formos.huub.domain.response.communityboard.ResponseCommunityBoardUser;
import com.formos.huub.domain.response.member.ResponseMemberProfile;
import com.formos.huub.domain.response.member.ResponsePublicDetailProfile;
import com.formos.huub.domain.response.portals.ResponseAboutPortalHost;
import com.formos.huub.mapper.authority.AuthorityMapper;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
    uses = {
        AuthorityMapper.class
    },
    unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MemberMapper {

    User toEntity(RequestMemberProfile requestMemberProfile);

    @Mapping(target = "id", ignore = true)
    void partialEntity(@MappingTarget User user, RequestMemberProfile requestMemberProfile);

    @Mapping(target = "id", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy =  NullValuePropertyMappingStrategy.IGNORE)
    void partialEntityIgnoreNull(@MappingTarget User user, RequestMemberProfile requestMemberProfile);

    ResponseMemberProfile toResponse(User user);

    @Mapping(target = "avatar", source = "imageUrl")
    @Mapping(target = "fullName", source = "normalizedFullName")
    ResponseCommunityBoardUser toResponseCommunityBoard(User user);

    ResponsePublicDetailProfile toResponsePublic(User user);

    ResponseAboutPortalHost toResponseBasic(User user);

}
