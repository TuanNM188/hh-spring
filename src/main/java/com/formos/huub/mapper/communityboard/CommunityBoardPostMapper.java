package com.formos.huub.mapper.communityboard;

import com.formos.huub.domain.entity.CommunityBoardPost;
import com.formos.huub.domain.entity.User;
import com.formos.huub.domain.request.communityboardpost.RequestCreateCommunityBoardPost;
import com.formos.huub.domain.request.communityboardpost.RequestUpdateCommunityBoardPost;
import com.formos.huub.domain.response.communityboardpost.IResponseCommunityBoardPost;
import com.formos.huub.domain.response.communityboardpost.ResponseCommunityBoardPost;
import com.formos.huub.domain.response.communityboardpost.ResponseCommunityBoardPostDetail;
import org.mapstruct.*;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CommunityBoardPostMapper {

    @Mapping(target = "isPin", constant = "false")
    @Mapping(target = "portalId", source = "portalId")
    @Mapping(target = "authorId", source = "authorId")
    CommunityBoardPost toEntity(RequestCreateCommunityBoardPost request, UUID portalId, UUID authorId);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    CommunityBoardPost partialUpdate(@MappingTarget CommunityBoardPost post, RequestUpdateCommunityBoardPost request);

    @Mapping(target = "totalComment", constant = "0")
    ResponseCommunityBoardPost toResponse(CommunityBoardPost request);


    ResponseCommunityBoardPost toResponse(User request);

    ResponseCommunityBoardPostDetail toResponse(IResponseCommunityBoardPost iPost);
}
