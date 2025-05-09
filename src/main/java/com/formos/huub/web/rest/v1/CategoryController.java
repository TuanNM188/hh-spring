package com.formos.huub.web.rest.v1;

import com.formos.huub.domain.request.categories.RequestCreateCategory;
import com.formos.huub.domain.request.categories.RequestUpdateCategory;
import com.formos.huub.domain.response.categories.ResponseCategories;
import com.formos.huub.framework.handler.model.ResponseData;
import com.formos.huub.framework.support.ResponseSupport;
import com.formos.huub.framework.validation.constraints.UUIDCheck;
import com.formos.huub.service.categories.CategoriesService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/categories")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryController {

    CategoriesService categoriesService;

    ResponseSupport responseSupport;

    @PreAuthorize("hasPermission(null, 'GET_CATEGORY_LIST')")
    @GetMapping
    public ResponseEntity<ResponseData> getAllCategory(@RequestParam(required = false) boolean hasServices) {
        List<ResponseCategories> categories = categoriesService.getAll();
        if (hasServices) {
            categories = categoriesService.getAllCategoriesAndServices();
        }
        return responseSupport.success(ResponseData.builder().data(categories).build());
    }

    @PreAuthorize("hasPermission(null, 'CREATE_CATEGORY')")
    @PostMapping
    public ResponseEntity<ResponseData> createCategories(@RequestBody @Valid RequestCreateCategory request) {
        var response = categoriesService.createCategories(request);
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @PreAuthorize("hasPermission(null, 'UPDATE_CATEGORY_DETAIL')")
    @PatchMapping("/{categoryId}")
    public ResponseEntity<ResponseData> updateCategories(
        @PathVariable @UUIDCheck String categoryId,
        @RequestBody @Valid RequestUpdateCategory request
    ) {
        var response = categoriesService.updateCategories(UUID.fromString(categoryId), request);
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @PreAuthorize("hasPermission(null, 'DELETE_CATEGORY')")
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<ResponseData> deleteCategory(@PathVariable @UUIDCheck String categoryId) {
        categoriesService.deleteCategory(UUID.fromString(categoryId));
        return responseSupport.success();
    }
}
