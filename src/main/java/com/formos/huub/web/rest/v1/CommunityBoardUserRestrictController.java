package com.formos.huub.web.rest.v1;

import com.formos.huub.domain.request.communityboarduserrestrict.RequestAddRestrictMember;
import com.formos.huub.domain.request.communityboarduserrestrict.RequestRemoveRestrictMember;
import com.formos.huub.framework.handler.model.ResponseData;
import com.formos.huub.framework.support.ResponseSupport;
import com.formos.huub.service.communityboard.CommunityBoardUserRestrictionService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/community-board-user-restrict")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommunityBoardUserRestrictController {

    ResponseSupport responseSupport;

    CommunityBoardUserRestrictionService restrictionService;

    @PostMapping
    @PreAuthorize("hasPermission(null, 'ADD_USER_RESTRICT')")
    public ResponseEntity<ResponseData> addRestrictMember(@RequestBody @Valid RequestAddRestrictMember request) {
        var result = restrictionService.addRestrictMember(request);
        return responseSupport.success(ResponseData.builder().data(result).build());
    }

    @PatchMapping
    @PreAuthorize("hasPermission(null, 'REMOVE_USER_RESTRICT')")
    public ResponseEntity<ResponseData> removeRestrictMember(@RequestBody @Valid RequestRemoveRestrictMember request) {
        restrictionService.removeRestrictMember(request);
        return responseSupport.success(ResponseData.builder().build());
    }

}
