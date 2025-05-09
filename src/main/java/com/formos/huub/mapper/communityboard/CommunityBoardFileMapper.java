package com.formos.huub.mapper.communityboard;

import com.formos.huub.domain.entity.CommunityBoardFile;
import com.formos.huub.domain.enums.CommunityBoardEntryTypeEnum;
import com.formos.huub.domain.request.communityboardpost.RequestCommunityBoardFile;
import com.formos.huub.domain.response.communityboard.ResponseCommunityBoardFile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CommunityBoardFileMapper {
    @Mapping(target = "type", source = "request.mediaType")
    @Mapping(target = "entryType", source = "entryType")
    @Mapping(target = "entryId", source = "entryId")
    @Mapping(target = "ownerId", source = "ownerId")
    @Mapping(target = "name", source = "request.name")
    @Mapping(target = "realName",
        expression = "java(com.formos.huub.framework.utils.StringUtils.getFileNameFromURL(request.getUrl()))")
    @Mapping(target = "path", source = "request.url")
    CommunityBoardFile toEntity(RequestCommunityBoardFile request, CommunityBoardEntryTypeEnum entryType, UUID entryId, UUID ownerId);

    @Mapping(target = "mediaType", source = "request.type")
    @Mapping(target = "url", source = "request.path")
    @Mapping(target = "name", source = "request.realName")
    ResponseCommunityBoardFile toResponse(CommunityBoardFile request);

    List<ResponseCommunityBoardFile> toResponses(List<CommunityBoardFile> request);
}
