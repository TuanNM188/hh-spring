package com.formos.huub.mapper.communityboard;

import com.formos.huub.domain.response.communityboard.IResponseCommunityBoardGroupSetting;
import com.formos.huub.domain.response.communityboard.ResponseCommunityBoardGroupSetting;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CommunityBoardGroupSettingMapper {

    List<ResponseCommunityBoardGroupSetting> toListResponseFromInterface(List<IResponseCommunityBoardGroupSetting> settings);

}
