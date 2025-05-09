package com.formos.huub.web.rest.v1.APIPublic;

import com.formos.huub.domain.request.communityresource.RequestSearchCommunityResource;
import com.formos.huub.framework.handler.model.ResponseData;
import com.formos.huub.framework.support.ResponseSupport;
import com.formos.huub.service.communitypartner.CommunityResourceService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/public/community-resources")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommunityResourcesPublicController {

    ResponseSupport responseSupport;
    CommunityResourceService communityResourceService;

    @GetMapping
    public ResponseEntity<ResponseData> getAllCommunityResourceInPortal(
        RequestSearchCommunityResource resource,
        @SortDefault(sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        var response = communityResourceService.searchCommunityResources(resource, pageable);
        return responseSupport.success(ResponseData.builder().data(response).build());
    }
}
