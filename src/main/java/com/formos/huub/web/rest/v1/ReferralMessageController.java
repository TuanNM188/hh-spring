package com.formos.huub.web.rest.v1;

import com.formos.huub.domain.request.directmessage.RequestSearchReferralMessage;
import com.formos.huub.framework.handler.model.ResponseData;
import com.formos.huub.framework.support.ResponseSupport;
import com.formos.huub.framework.utils.HeaderUtils;
import com.formos.huub.framework.utils.UUIDUtils;
import com.formos.huub.service.referralmessage.ReferralMessageService;
import jakarta.servlet.http.HttpServletRequest;
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
@RequestMapping("/referral-message")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReferralMessageController {

    private final ResponseSupport responseSupport;

    private final ReferralMessageService referralMessageService;


    @PostMapping("/search")
    @PreAuthorize("hasPermission(null, 'SEARCH_REFERRAL_MESSAGE_MANAGEMENTS')")
    public ResponseEntity<ResponseData> searchReferralMessages(@Valid @RequestBody RequestSearchReferralMessage request, HttpServletRequest httpServletRequest) {

        var response = referralMessageService.searchReferralMessages(request,  HeaderUtils.getTimezone(httpServletRequest));
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasPermission(null, 'GET_REFERRAL_MESSAGE_BY_ID')")
    public ResponseEntity<ResponseData> getDetailById(@PathVariable String id) {

        var response = referralMessageService.getDetail(UUIDUtils.toUUID(id));
        return responseSupport.success(ResponseData.builder().data(response).build());
    }
}
