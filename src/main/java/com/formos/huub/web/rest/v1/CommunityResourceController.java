package com.formos.huub.web.rest.v1;

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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/community-resources")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommunityResourceController {


    CommunityResourceService communityResourceService;

    ResponseSupport responseSupport;

    @GetMapping()
    @PreAuthorize("hasPermission(null, 'GET_COMMUNITY_RESOURCE_LIST')")
    public ResponseEntity<ResponseData> getAllCommunityResourceInPortal(RequestSearchCommunityResource resource,
                                                                @SortDefault(sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        var response = communityResourceService.searchCommunityResources(resource, pageable);
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasPermission(null, 'GET_DETAIL_COMMUNITY_RESOURCE')")
    public ResponseEntity<ResponseData> getDetailCommunityResource(@PathVariable String id) {
        var response = communityResourceService.getDetailCommunityResource(UUID.fromString(id));
        return responseSupport.success(ResponseData.builder().data(response).build());
    }
}
