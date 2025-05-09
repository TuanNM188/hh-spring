package com.formos.huub.web.rest.v1;

import com.formos.huub.domain.request.advisementcategory.RequestAdvisementCategory;
import com.formos.huub.framework.handler.model.ResponseData;
import com.formos.huub.framework.message.MessageHelper;
import com.formos.huub.framework.message.model.Message;
import com.formos.huub.framework.support.ResponseSupport;
import com.formos.huub.framework.validation.constraints.UUIDCheck;
import com.formos.huub.service.advisementcategory.AdvisementCategoryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/advisement-categories")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdvisementCategoryController {

    AdvisementCategoryService advisementCategoryService;
    ResponseSupport responseSupport;

    @GetMapping("/{technicalAdvisorId}")
    @PreAuthorize("hasPermission(null, 'GET_ADVISEMENT_CATEGORIES_BY_TECHNICAL_ADVISOR_ID')")
    public ResponseEntity<ResponseData> getAllAdvisementCategoryByTechnicalAdvisorId(@PathVariable @UUIDCheck String technicalAdvisorId) {
        return responseSupport.success(ResponseData.builder().data(advisementCategoryService.getAllAdvisementCategoryByTechnicalAdvisorId(UUID.fromString(technicalAdvisorId))).build());
    }

    @PatchMapping("/{technicalAdvisorId}")
    @PreAuthorize("hasPermission(null, 'UPDATE_ADVISEMENT_CATEGORIES_BY_TECHNICAL_ADVISOR_ID')")
    public ResponseEntity<ResponseData> updateUserAdvisementCategory(@PathVariable @UUIDCheck String technicalAdvisorId, @Valid @RequestBody @Size(max = 3) List<RequestAdvisementCategory> request) {
        advisementCategoryService.updateUserAdvisementCategory(UUID.fromString(technicalAdvisorId), request);
        return responseSupport.success(MessageHelper.getMessage(Message.Keys.I0001));
    }

}
