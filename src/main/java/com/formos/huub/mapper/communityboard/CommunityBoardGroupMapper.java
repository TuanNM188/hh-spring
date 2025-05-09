package com.formos.huub.mapper.communityboard;

import com.formos.huub.domain.entity.CommunityBoardGroup;
import com.formos.huub.domain.entity.CommunityBoardGroupMember;
import com.formos.huub.domain.request.communityboard.RequestCreateCommunityBoardGroup;
import com.formos.huub.domain.request.communityboard.RequestUpdateCommunityBoardGroup;
import com.formos.huub.domain.response.communityboard.ResponseCommunityBoardGroup;
import com.formos.huub.domain.response.communityboard.ResponseResultJoinGroup;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CommunityBoardGroupMapper {

    @Mapping(target = "groupName", source = "request.detail.groupName")
    @Mapping(target = "description", source = "request.detail.description")
    @Mapping(target = "groupAvatar", source = "request.photo.groupAvatar")
    @Mapping(target = "coverPhoto", source = "request.photo.coverPhoto")
    CommunityBoardGroup toEntity(RequestCreateCommunityBoardGroup request);

    @Mapping(target = "groupName", source = "request.detail.groupName")
    @Mapping(target = "description", source = "request.detail.description")
    @Mapping(target = "groupAvatar", source = "request.photo.groupAvatar")
    @Mapping(target = "coverPhoto", source = "request.photo.coverPhoto")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    CommunityBoardGroup partialUpdate(@MappingTarget CommunityBoardGroup communityBoardGroup, RequestUpdateCommunityBoardGroup request);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "detail.groupName", source = "groupName")
    @Mapping(target = "detail.description", source = "description")
    @Mapping(target = "photo.groupAvatar", source = "groupAvatar")
    @Mapping(target = "photo.coverPhoto", source = "coverPhoto")
    ResponseCommunityBoardGroup toResponseDetail(CommunityBoardGroup communityBoardGroup);

    ResponseResultJoinGroup toResponseMemberDetail(CommunityBoardGroupMember communityBoardGroup);

}
