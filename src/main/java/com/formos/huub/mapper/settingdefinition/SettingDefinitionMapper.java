/**
 * ***************************************************
 * * Description :
 * * File        : SettingDefinitionMapper
 * * Author      : Hung Tran
 * * Date        : Oct 17, 2024
 * ***************************************************
 **/
package com.formos.huub.mapper.settingdefinition;

import com.formos.huub.domain.entity.SettingDefinition;
import com.formos.huub.domain.response.settingdefinition.ResponseSettingDefinitionDetail;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SettingDefinitionMapper {
    ResponseSettingDefinitionDetail toResponse(SettingDefinition settingDefinition);

    List<ResponseSettingDefinitionDetail> toListResponse(List<SettingDefinition> settingDefinitions);
}
