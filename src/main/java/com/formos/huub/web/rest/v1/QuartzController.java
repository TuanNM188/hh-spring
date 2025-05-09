package com.formos.huub.web.rest.v1;

import com.formos.huub.framework.handler.model.ResponseData;
import com.formos.huub.framework.support.ResponseSupport;
import com.formos.huub.service.quartz.SchedulerJobService;
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
@RequestMapping("/quartz")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class QuartzController {

    ResponseSupport responseSupport;

    SchedulerJobService schedulerJobService;

    @GetMapping
    @PreAuthorize("hasPermission(null, 'GET_COMMUNITY_BOARD_POSTS')")
    public ResponseEntity<ResponseData> getAllCommunityResourceInPortal() {
        var response = schedulerJobService.getAllJobList();
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

}
