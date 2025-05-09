package com.formos.huub.web.rest.v1.APIPublic;

import com.formos.huub.framework.handler.model.ResponseData;
import com.formos.huub.framework.support.ResponseSupport;
import com.formos.huub.service.portals.PortalService;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/public/apply-supports")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ApplySupportPublicController {

    ResponseSupport responseSupport;
    PortalService portalService;

    @GetMapping("/{portalId}/apply-1-1-support-screen")
    public ResponseEntity<ResponseData> answerAboutScreenConfigurationByPortal(@PathVariable String portalId) {
        var response = portalService.getApply11SupportScreenConfigurations(UUID.fromString(portalId));
        return responseSupport.success(ResponseData.builder().data(response).build());
    }
}

