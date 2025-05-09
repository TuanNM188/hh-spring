package com.formos.huub.web.rest.v1;

import com.formos.huub.framework.handler.model.ResponseData;
import com.formos.huub.framework.support.ResponseSupport;
import com.formos.huub.service.appointment.AppointmentService;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/appointment")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AppointmentController {

    ResponseSupport responseSupport;

    AppointmentService appointmentService;

    @GetMapping("/{id}")
    public ResponseEntity<ResponseData> getDetail(@PathVariable String id) {
        var response = appointmentService.getDetail(UUID.fromString(id));
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @PostMapping("/cancel/{id}")
    @PreAuthorize("hasPermission(null, 'CANCEL_APPOINTMENT')")
    public ResponseEntity<ResponseData> cancelAppointment(@PathVariable String id) {
        var response = appointmentService.cancelAppointment(UUID.fromString(id));
        return responseSupport.success(ResponseData.builder().data(response).build());
    }
}
