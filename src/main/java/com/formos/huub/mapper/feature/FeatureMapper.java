package com.formos.huub.mapper.feature;

import com.formos.huub.domain.entity.Feature;
import com.formos.huub.domain.response.feature.IResponseFeatureNavigation;
import com.formos.huub.domain.response.feature.IResponseFeaturePortalSetting;
import com.formos.huub.domain.response.feature.ResponseFeatureNavigation;
import com.formos.huub.domain.response.portals.ResponsePortalFeature;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FeatureMapper {

    Feature fromIdToEntity(String id);

    List<ResponsePortalFeature> toListResponse(List<IResponseFeaturePortalSetting> featurePortalSettings);

    ResponseFeatureNavigation toResponse(IResponseFeatureNavigation iResponseFeatureNavigation);
}
