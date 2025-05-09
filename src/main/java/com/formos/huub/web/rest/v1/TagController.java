package com.formos.huub.web.rest.v1;

import com.formos.huub.framework.handler.model.ResponseData;
import com.formos.huub.framework.support.ResponseSupport;
import com.formos.huub.service.tag.TagService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tags")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TagController {

    ResponseSupport responseSupport;

    TagService tagService;

    @GetMapping("/meta")
    @PreAuthorize("hasPermission(null, 'GET_TAG_PORTAL_META')")
    public ResponseEntity<ResponseData> getAllPortalMeta() {
        return responseSupport.success(ResponseData.builder().data(tagService.getAll()).build());
    }
}
