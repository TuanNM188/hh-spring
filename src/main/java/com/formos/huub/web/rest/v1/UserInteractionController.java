package com.formos.huub.web.rest.v1;

import com.formos.huub.domain.request.userinteraction.RequestUserInteraction;
import com.formos.huub.framework.enums.LanguageEnum;
import com.formos.huub.framework.handler.model.ResponseData;
import com.formos.huub.framework.support.ResponseSupport;
import com.formos.huub.service.userinteractions.UserInteractionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user-interactions")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserInteractionController {

    ResponseSupport responseSupport;

    UserInteractionService userInteractionService;

    @PostMapping()
    public ResponseEntity<ResponseData> saveUserInteraction(@RequestBody RequestUserInteraction request) {
        userInteractionService.saveUserInteraction(request);
        return responseSupport.success(ResponseData.builder().build());
    }

    @PostMapping("/language/{languageCode}")
    public ResponseEntity<ResponseData> saveUserPreferredLanguage(@PathVariable String languageCode) {
        var response = userInteractionService.saveUserPreferredLanguage(languageCode);
        return responseSupport.success(ResponseData.builder().data(response).build());
    }
}
