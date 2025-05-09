package com.formos.huub.service.termsandconditions;

import com.formos.huub.domain.enums.TermsAndConditionsTypeEnum;
import com.formos.huub.domain.response.termsandconditions.ResponseTermsAndConditions;
import com.formos.huub.framework.base.BaseService;
import com.formos.huub.mapper.termsandconditions.TermsAndConditionsMapper;
import com.formos.huub.repository.TermsAndConditionsRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TermsAndConditionsService extends BaseService {

    TermsAndConditionsRepository termsAndConditionsRepository;

    TermsAndConditionsMapper termsAndConditionsMapper;

    public ResponseTermsAndConditions getTermsAndConditionsText(TermsAndConditionsTypeEnum type) {
        return termsAndConditionsRepository.getTermsAndConditionsByType(type)
            .map(termsAndConditionsMapper::toResponse)
            .orElse(new ResponseTermsAndConditions());
    }

}
