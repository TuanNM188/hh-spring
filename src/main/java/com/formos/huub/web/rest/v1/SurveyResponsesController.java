package com.formos.huub.web.rest.v1;

import com.formos.huub.framework.handler.model.ResponseData;
import com.formos.huub.framework.support.ResponseSupport;
import com.formos.huub.service.survey.SurveyResponsesService;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/survey-responses")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SurveyResponsesController {

    ResponseSupport responseSupport;

    SurveyResponsesService surveyResponsesService;

    @GetMapping("/{id}")
    public ResponseEntity<ResponseData> getDetail(@PathVariable String id) {
        var response = surveyResponsesService.getDetail(UUID.fromString(id));
        return responseSupport.success(ResponseData.builder().data(response).build());
    }
}
