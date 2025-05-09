package com.formos.huub.service.webhookevent;

import com.formos.huub.domain.entity.WebhookEvent;
import com.formos.huub.domain.enums.ProviderEnum;
import com.formos.huub.domain.enums.WebhookEventStatusEnum;
import com.formos.huub.domain.response.webhook.eventbrite.ResponseEventbritePayload;
import com.formos.huub.framework.base.BaseService;
import com.formos.huub.framework.constant.AppConstants;
import com.formos.huub.framework.constant.EventbriteWebhookType;
import com.formos.huub.repository.WebhookEventRepository;
import com.formos.huub.service.calendarevent.CalendarEventService;
import com.formos.huub.service.eventregistration.EventRegistrationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WebhookEventService extends BaseService {
    private final WebhookEventRepository webhookEventRepository;
    private final CalendarEventService calendarEventService;
    private final EventRegistrationService eventRegistrationService;
    private final WebhookEventSaveAllService webhookEventSaveAllService;

    public void createEventbriteWebhook(Map<String, Object> payload, String eventType, String delivery) {
        if (Objects.isNull(payload) || Objects.isNull(eventType) || Objects.isNull(delivery)) {
            return;
        }
        var webhookEvent = WebhookEvent.builder()
            .provider(ProviderEnum.EVENTBRITE)
            .status(WebhookEventStatusEnum.PENDING)
            .eventType(eventType)
            .eventId(delivery)
            .payload(ResponseEventbritePayload.toJsonFromPayload(payload))
            .createdDate(Instant.now())
            .lastModifiedDate(Instant.now())
            .build();
        webhookEventRepository.save(webhookEvent);
    }

    @Scheduled(fixedRate = 5000) // 5 seconds
    public void processWebhookData() {
        handleEventbriteEvents();
    }

    @Scheduled(fixedRate = 8000) // 8 seconds
    public void processRetryWebhookData() {
        retryHandleEventbriteEvents();
    }

    @Scheduled(cron = "0 0 1 * * ?")
    public void deleteOldWebhookEvent() {
        // Get the current date and subtract 30 days
        log.info("Start delete old webhook event at: {}", Instant.now().toString());
        LocalDateTime dateThreshold = LocalDateTime.now().minusDays(30);
        Instant instantThreshold = dateThreshold.atZone(ZoneId.systemDefault()).toInstant();

        // Call the repository to delete records older than 30 days
        webhookEventRepository.deleteByTimestampBefore(instantThreshold);
        log.info("End delete old webhook event");
    }

    public void handleEventbriteEvents() {
        synchronized (this) {
            var eventbriteEvents = webhookEventRepository.findTop10ByProviderAndStatusOrderByCreatedDateDesc(ProviderEnum.EVENTBRITE, WebhookEventStatusEnum.PENDING);
            // If there is no event then return
            if (eventbriteEvents.isEmpty()) {
                return;
            }
            log.info("Handle webhook events");
            eventbriteEvents.forEach(this::handleEventbriteEvent);
            // Save all
            webhookEventSaveAllService.saveEvent(eventbriteEvents);
        }
    }

    public void retryHandleEventbriteEvents() {
        synchronized (this) {
            var eventbriteEvents = webhookEventRepository.findTop10ByProviderAndStatusAndNextRetryAtIsBeforeOrderByCreatedDateDesc(
                ProviderEnum.EVENTBRITE,
                WebhookEventStatusEnum.FAILED,
                Instant.now()
            );
            // If there is no event then return
            if (eventbriteEvents.isEmpty()) {
                return;
            }
            log.info("Retry Handle webhook events");
            eventbriteEvents.forEach(this::handleEventbriteEvent);
            // Save all
            webhookEventSaveAllService.saveEvent(eventbriteEvents);
        }
    }

    private void handleEventbriteEvent(WebhookEvent eventbrite) {
        var type = eventbrite.getEventType();
        var eventbritePayload = ResponseEventbritePayload.fromJson(eventbrite.getPayload());
        try {
            switch (type) {
                case EventbriteWebhookType.EVENT_UPDATED:
                    calendarEventService.handleSaveEventbrite(eventbritePayload.getApiUrl());
                    break;
                case EventbriteWebhookType.EVENT_UNPUBLISHED:
                    calendarEventService.handleCancelEvent(eventbritePayload.getApiUrl());
                    break;
                case EventbriteWebhookType.ATTENDEE_UPDATED:
                    eventRegistrationService.handleSaveEventAttendee(eventbritePayload.getApiUrl());
                    break;
                default:
                    log.warn("Unhandled Eventbrite Webhook: {}", type);
            }
            // Mark as processed
            eventbrite.setStatus(WebhookEventStatusEnum.SUCCESS);
            eventbrite.setNextRetryAt(null);
        } catch (Exception e) {
            if (eventbrite.getRetryCount() >= AppConstants.RETRIES_LIMIT) {
                eventbrite.setStatus(WebhookEventStatusEnum.FAILED_PERMANENTLY);
                eventbrite.setErrorDetail(e.getMessage());
                log.error(type);
                log.error(e.getMessage(), e);
                log.error(eventbrite.getPayload());
                return;
            }
            eventbrite.setStatus(WebhookEventStatusEnum.FAILED);
            eventbrite.setRetryCount(eventbrite.getRetryCount() + 1);
            eventbrite.setNextRetryAt(Instant.now().plusSeconds(eventbrite.getRetryCount() * AppConstants.SECONDS_OF_A_MINUTE));
        }
    }
}
