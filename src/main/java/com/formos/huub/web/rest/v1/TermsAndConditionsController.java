package com.formos.huub.web.rest.v1;

import com.formos.huub.domain.enums.TermsAndConditionsTypeEnum;
import com.formos.huub.domain.response.termsandconditions.ResponseTermsAndConditions;
import com.formos.huub.framework.handler.model.ResponseData;
import com.formos.huub.framework.support.ResponseSupport;
import com.formos.huub.service.termsandconditions.TermsAndConditionsService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/terms-and-conditions")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TermsAndConditionsController {

    TermsAndConditionsService termsAndConditionsService;

    ResponseSupport responseSupport;

    @GetMapping("")
    public ResponseEntity<ResponseData> findTermsAndConditionsByType(@RequestParam String type) {
        ResponseTermsAndConditions response = termsAndConditionsService.getTermsAndConditionsText(TermsAndConditionsTypeEnum.valueOf(type));
        return responseSupport.success(ResponseData.builder().data(response).build());
    }


}
