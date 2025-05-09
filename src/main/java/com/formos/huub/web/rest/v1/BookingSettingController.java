package com.formos.huub.web.rest.v1;

import com.formos.huub.domain.enums.CalendarTypeEnum;
import com.formos.huub.domain.request.bookingsetting.RequestBookingSetting;
import com.formos.huub.framework.handler.model.ResponseData;
import com.formos.huub.framework.support.ResponseSupport;
import com.formos.huub.framework.validation.constraints.UUIDCheck;
import com.formos.huub.service.bookingsetting.BookingSettingService;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/settings")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingSettingController {

    ResponseSupport responseSupport;

    BookingSettingService settingService;

    @GetMapping("/{userOrTechnicalAdvisorId}/booking")
    @PreAuthorize("hasPermission(null, 'GET_SETTINGS_BOOKING_BY_TECHNICAL_ADVISOR_ID')")
    public ResponseEntity<ResponseData> getAllServices(@PathVariable @UUIDCheck String userOrTechnicalAdvisorId) throws Exception {
        return responseSupport.success(
            ResponseData.builder().data(settingService.getBookingSettingOfUser(UUID.fromString(userOrTechnicalAdvisorId))).build()
        );
    }

    @PostMapping("/booking")
    @PreAuthorize("hasPermission(null, 'CREATE_SETTINGS_BOOKING')")
    public ResponseEntity<ResponseData> saveSetting(@RequestBody RequestBookingSetting request) throws IOException, URISyntaxException {
        settingService.updateBookingSetting(request);
        return responseSupport.success();
    }

    @GetMapping(value = "/sub-calendars")
    @PreAuthorize("hasPermission(null, 'GET_SETTINGS_SUB_CALENDARS')")
    public ResponseEntity<ResponseData> getCalendarList(
        @RequestParam String accessToken,
        @RequestParam String id,
        @RequestParam String calendarType
    ) {
        var response = settingService.getCalendarList(UUID.fromString(id), CalendarTypeEnum.valueOf(calendarType), accessToken);
        return responseSupport.success(ResponseData.builder().data(response).build());
    }
}
