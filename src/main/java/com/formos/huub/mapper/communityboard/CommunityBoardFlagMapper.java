package com.formos.huub.mapper.communityboard;

import com.formos.huub.domain.entity.CommunityBoardFlag;
import com.formos.huub.domain.request.communityboardpost.RequestMemberFlagContent;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CommunityBoardFlagMapper {

    CommunityBoardFlag toEntity(RequestMemberFlagContent request);

}
