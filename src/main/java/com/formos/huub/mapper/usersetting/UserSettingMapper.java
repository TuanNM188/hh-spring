package com.formos.huub.mapper.usersetting;

import com.formos.huub.domain.entity.UserSetting;
import com.formos.huub.domain.response.usersetting.ResponseUserSetting;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    builder = @Builder(disableBuilder = true),
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface UserSettingMapper {

    ResponseUserSetting toResponse(UserSetting userSetting);

    List<ResponseUserSetting> toListResponse(List<UserSetting> userSettings);

}
