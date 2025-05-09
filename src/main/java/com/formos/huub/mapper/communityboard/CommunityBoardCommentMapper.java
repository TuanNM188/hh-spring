package com.formos.huub.mapper.communityboard;

import com.formos.huub.domain.entity.CommunityBoardComment;
import com.formos.huub.domain.request.communityboardcomment.RequestCreateCommunityBoardComment;
import com.formos.huub.domain.request.communityboardcomment.RequestUpdateCommunityBoardComment;
import com.formos.huub.domain.response.communityboardcomment.IResponseCommunityBoardComment;
import com.formos.huub.domain.response.communityboardcomment.ResponseCommunityBoardComment;
import com.formos.huub.domain.response.communityboardcomment.ResponseCommunityBoardCommentDetail;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CommunityBoardCommentMapper {

    CommunityBoardComment toEntity(RequestCreateCommunityBoardComment request);

    @Mapping(target = "postId", ignore = true)
    @Mapping(target = "authorId", ignore = true)
    @Mapping(target = "parentId", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    CommunityBoardComment partialUpdate(@MappingTarget CommunityBoardComment comment, RequestUpdateCommunityBoardComment request);

    @Mapping(target = "totalCommentChild", constant = "0")
    @Mapping(target = "totalLike", constant = "0")
    @Mapping(target = "createAt", source = "createdDate")
    ResponseCommunityBoardComment toResponse(CommunityBoardComment request);

    ResponseCommunityBoardCommentDetail toResponse(IResponseCommunityBoardComment iComment);
}
