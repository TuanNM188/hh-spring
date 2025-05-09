package com.formos.huub.web.rest.v1;

import com.formos.huub.framework.handler.model.ResponseData;
import com.formos.huub.framework.support.ResponseSupport;
import com.formos.huub.service.blockeduser.BlockedUserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/blocked-users")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BlockedUserController {

    ResponseSupport responseSupport;

    BlockedUserService blockedUserService;

    @GetMapping
    public ResponseEntity<ResponseData> getBlockedUserWithPageable() {
        return responseSupport.success(ResponseData.builder().data(blockedUserService.getBlockedUserByCurrentUser()).build());
    }

}
