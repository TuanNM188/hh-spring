package com.formos.huub.web.rest.v1;

import com.formos.huub.domain.request.language.RequestCreateLanguage;
import com.formos.huub.domain.request.language.RequestUpdateLanguages;
import com.formos.huub.framework.handler.model.ResponseData;
import com.formos.huub.framework.support.ResponseSupport;
import com.formos.huub.framework.validation.constraints.UUIDCheck;
import com.formos.huub.service.language.LanguageService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/languages")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LanguagesController {

    LanguageService languageService;

    ResponseSupport responseSupport;

    @GetMapping
    @PreAuthorize("hasPermission(null, 'GET_LANGUAGE_LIST')")
    public ResponseEntity<ResponseData> getAllLanguages() {
        return responseSupport.success(ResponseData.builder().data(languageService.getAll()).build());
    }

    @PostMapping
    @PreAuthorize("hasPermission(null, 'CREATE_LANGUAGE')")
    public ResponseEntity<ResponseData> createLanguages(@RequestBody @Valid RequestCreateLanguage request) {
       var response =  languageService.createLanguages(request);
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @PatchMapping("/{languageId}")
    @PreAuthorize("hasPermission(null, 'UPDATE_LANGUAGE_DETAIL')")
    public ResponseEntity<ResponseData> updateLanguages(@PathVariable @UUIDCheck String languageId,
                                                        @RequestBody @Valid RequestUpdateLanguages request) {
        var response = languageService.updateLanguages(UUID.fromString(languageId), request);
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @DeleteMapping("/{languageId}")
    @PreAuthorize("hasPermission(null, 'DELETE_LANGUAGE')")
    public ResponseEntity<ResponseData> deleteLanguages(@PathVariable @UUIDCheck String languageId) {
        languageService.deleteLanguages(UUID.fromString(languageId));
        return responseSupport.success(ResponseData.builder().data("SUCCESS").build());
    }
}
