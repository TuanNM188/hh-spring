package com.formos.huub.web.rest.v1;

import com.formos.huub.domain.request.translate.RequestTranslate;
import com.formos.huub.framework.base.BaseController;
import com.formos.huub.framework.handler.model.ResponseData;
import com.formos.huub.framework.support.ResponseSupport;
import com.formos.huub.service.translate.TranslateService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/translate")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TranslateController extends BaseController {

    ResponseSupport responseSupport;

    TranslateService translateService;

    @PostMapping
    public ResponseEntity<ResponseData> translate(@Valid @RequestBody RequestTranslate request) {
        var response = translateService.translate(request);
        return responseSupport.success(ResponseData.builder().data(response).build());
    }
}
