package com.formos.huub.web.rest.v1;

import com.formos.huub.domain.request.communityboardpost.*;
import com.formos.huub.framework.handler.model.ResponseData;
import com.formos.huub.framework.support.ResponseSupport;
import com.formos.huub.framework.utils.UUIDUtils;
import com.formos.huub.framework.validation.constraints.UUIDCheck;
import com.formos.huub.service.communityboard.CommunityBoardFlagService;
import com.formos.huub.service.communityboard.CommunityBoardPostService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/community-board-flag")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommunityBoardFlagController {

    ResponseSupport responseSupport;

    CommunityBoardFlagService communityBoardFlagService;

    @PostMapping()
    @PreAuthorize("hasPermission(null, 'CREATE_COMMUNITY_BOARD_FLAG')")
    public ResponseEntity<ResponseData> createFlagPost(@RequestBody @Valid RequestMemberFlagContent request) {
        communityBoardFlagService.createFlagPost(request);
        return responseSupport.success(ResponseData.builder().build());
    }

    @PreAuthorize("hasPermission(null, 'GET_COMMUNITY_BOARD_FLAG')")
    @GetMapping("/{flagId}")
    public ResponseEntity<ResponseData> getDetailPost(@PathVariable @UUIDCheck String flagId) {
        var response = communityBoardFlagService.getDetailFlag(UUIDUtils.convertToUUID(flagId));
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

}
