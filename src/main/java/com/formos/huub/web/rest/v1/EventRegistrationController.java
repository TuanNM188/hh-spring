package com.formos.huub.web.rest.v1;

import com.formos.huub.domain.request.businessowner.RequestSearchEventRegistrations;
import com.formos.huub.framework.handler.model.ResponseData;
import com.formos.huub.framework.support.ResponseSupport;
import com.formos.huub.framework.utils.HeaderUtils;
import com.formos.huub.service.eventregistration.EventRegistrationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/event-registrations")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EventRegistrationController {

    ResponseSupport responseSupport;

    EventRegistrationService eventRegistrationService;

    @PostMapping("/search")
    @PreAuthorize("hasPermission(null, 'SEARCH_EVENT_REGISTRATION_LIST')")
    public ResponseEntity<ResponseData> getAllEventRegistrationWithPageable(@Valid @RequestBody RequestSearchEventRegistrations request,
                                                                            HttpServletRequest httpServletRequest) {
        request.setTimezone(HeaderUtils.getTimezone(httpServletRequest));
        return responseSupport.success(ResponseData.builder().data(eventRegistrationService.searchEventRegistrations(request)).build());
    }

}
