package com.formos.huub.service.feature;

import com.formos.huub.domain.constant.FeatureConstant;
import com.formos.huub.domain.enums.FeatureCodeEnum;
import com.formos.huub.domain.response.feature.IResponseFeatureNavigation;
import com.formos.huub.domain.response.feature.IResponseFeaturePortalSetting;
import com.formos.huub.domain.response.feature.ResponseFeatureNavigation;
import com.formos.huub.framework.base.BaseService;
import com.formos.huub.framework.utils.ObjectUtils;
import com.formos.huub.mapper.feature.FeatureMapper;
import com.formos.huub.repository.FeatureRepository;
import com.formos.huub.repository.PortalFeatureRepository;
import com.formos.huub.security.AuthoritiesConstants;
import com.formos.huub.security.SecurityUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FeatureService extends BaseService {

    FeatureRepository featureRepository;

    PortalFeatureRepository portalFeatureRepository;

    FeatureMapper featureMapper;

    Map<FeatureCodeEnum, String> GROUP_CODE_PORTAL_INTAKE_QUESTION = FeatureConstant.GROUP_CODE_PORTAL_INTAKE_QUESTION;

    /**
     * get Feature Navigation
     *
     * @param featureCode FeatureCodeEnum
     * @param portalId    String
     * @return List<IResponseFeatureNavigation>
     */
    public List<ResponseFeatureNavigation> getFeatureNavigation(FeatureCodeEnum featureCode, String portalId) {
        if (ObjectUtils.isEmpty(portalId)) {
            return featureRepository.getAllByParentCode(featureCode).stream().map(featureMapper::toResponse).toList();
        }
        List<FeatureCodeEnum> excludeFeatureCodes = new ArrayList<>();
        if (SecurityUtils.hasCurrentUserAnyOfAuthorities(AuthoritiesConstants.PORTAL_HOST)) {
            excludeFeatureCodes.add(FeatureCodeEnum.FEATURES);
        }
        List<FeatureCodeEnum> includeFeatureCodes = new ArrayList<>();
        if (FeatureCodeEnum.PORTALS_INTAKE_QUESTIONS.equals(featureCode)) {
            includeFeatureCodes.add(FeatureCodeEnum.PROGRAM_DETAILS);
        }
        return portalFeatureRepository.getAllByParentCodeAndPortalId(featureCode, UUID.fromString(portalId), excludeFeatureCodes, includeFeatureCodes)
            .stream()
            .map(ele -> buildObjectResponse(ele, featureCode))
            .toList();
    }

    private ResponseFeatureNavigation buildObjectResponse(IResponseFeatureNavigation data, FeatureCodeEnum featureCode) {
        return featureMapper.toResponse(data);
    }

    /**
     * Get all feature portal setting
     *
     * @return List<IResponseFeatureSetting>
     */
    public List<IResponseFeaturePortalSetting> getAllFeaturePortalSetting() {
        return featureRepository.getAllFeaturePortalSetting();
    }

}
