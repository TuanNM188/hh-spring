package com.formos.huub.web.rest.webhook;

import com.formos.huub.framework.base.BaseController;
import com.formos.huub.framework.handler.model.ResponseData;
import com.formos.huub.framework.properties.WebhookKeyProperties;
import com.formos.huub.framework.support.ResponseSupport;
import com.formos.huub.service.webhookevent.WebhookEventService;
import com.formos.huub.web.cronjob.CalendarEventCronJob;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/eventbrites")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EventbriteWebhookController extends BaseController {

    WebhookEventService webhookEventService;
    CalendarEventCronJob calendarEventCronJob;
    WebhookKeyProperties webhookKeyProperties;

    ResponseSupport responseSupport;

    @PostMapping("/{apiKey}")
    public ResponseEntity<String> handleEventbriteWebhook(
        @PathVariable String apiKey,
        @RequestBody Map<String, Object> payload,
        @RequestHeader(value = "X-Eventbrite-Event") String eventType,
        @RequestHeader(value = "X-Eventbrite-Delivery") String delivery
    ) {
        if (!webhookKeyProperties.getEventbrite().equals(apiKey)) {
            return createErrorResponse();
        }
        log.info("Received Webhook from Eventbrite: {}", eventType);
        log.info("Payload: {}", payload.toString());
        webhookEventService.createEventbriteWebhook(payload, eventType, delivery);
        return createSuccessResponse();
    }

    private ResponseEntity<String> createErrorResponse() {
        String errorJson = """
            {
                "error": "Authentication failed",
                "detail": "Invalid API key."
            }
            """;
        return ResponseEntity
            .badRequest()
            .contentType(MediaType.APPLICATION_JSON)
            .body(errorJson);
    }

    private ResponseEntity<String> createSuccessResponse() {
        String responseJson = """
            {
                "status": "ok"
            }""";
        return ResponseEntity
            .ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(responseJson);
    }

    @PostMapping("/test-sync-huub-event-api")
    public ResponseEntity<ResponseData> testSyncHuubEvent() {
        calendarEventCronJob.jobScheduleSyncHuubEvent();
        return responseSupport.success(ResponseData.builder().build());
    }

    @PostMapping("/test-sync-huub-event-registration-api")
    public ResponseEntity<ResponseData> testSyncHuubEventRegistration() {
        calendarEventCronJob.jobScheduleSyncHuubEventRegistrations();
        return responseSupport.success(ResponseData.builder().build());
    }

}
