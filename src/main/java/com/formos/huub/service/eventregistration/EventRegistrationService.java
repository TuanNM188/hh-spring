package com.formos.huub.service.eventregistration;

import com.formos.huub.domain.constant.BusinessConstant;
import com.formos.huub.domain.entity.CalendarEvent;
import com.formos.huub.domain.entity.EventRegistration;
import com.formos.huub.domain.enums.EventStatusEnum;
import com.formos.huub.domain.request.businessowner.RequestSearchEventRegistrations;
import com.formos.huub.domain.request.project.RequestSearchProject;
import com.formos.huub.domain.response.eventregistration.ResponseEventRegistration;
import com.formos.huub.domain.response.member.IResponseBusinessOwnerEmail;
import com.formos.huub.domain.response.webhook.eventbrite.ResponseEventbritePayload;
import com.formos.huub.framework.context.PortalContextHolder;
import com.formos.huub.framework.utils.ObjectUtils;
import com.formos.huub.framework.utils.PageUtils;
import com.formos.huub.framework.utils.StringUtils;
import com.formos.huub.mapper.eventregistration.EventRegistrationMapper;
import com.formos.huub.repository.BusinessOwnerRepository;
import com.formos.huub.repository.CalendarEventRepository;
import com.formos.huub.repository.EventRegistrationRepository;
import com.formos.huub.service.externalcalendar.EventbriteService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
@EnableAsync(proxyTargetClass = true)
public class EventRegistrationService {

    EventRegistrationRepository eventRegistrationRepository;
    CalendarEventRepository calendarEventRepository;
    BusinessOwnerRepository businessOwnerRepository;
    EventbriteService eventbriteService;
    EventRegistrationMapper eventRegistrationMapper;


    /**
     * Event Registrations by term and condition
     *
     * @param request RequestSearchEventRegistrations
     * @return Map<String, Object> eventRegistrations
     */
    public Map<String, Object> searchEventRegistrations(RequestSearchEventRegistrations request) {
        var sort = !ObjectUtils.isEmpty(request.getSort()) ? request.getSort() : "er.registration_date,desc";
        var pageable = PageRequest.of(request.getPage(), request.getSize(), PageUtils.createSort(sort));
        if(Objects.isNull(request.getPortalId())){
            request.setPortalId(PortalContextHolder.getPortalId());
        }
        HashMap<String, String> sortMap = new HashMap<>();
        sortMap.put("businessOwnerName", "u.normalized_full_name");
        sortMap.put("startTime", "ce.start_time");
        sortMap.put("subject", "ce.subject");
        sortMap.put("portalName", "p.platform_name");

        sortMap.put(BusinessConstant.TIMEZONE_KEY, request.getTimezone());
        request.setSearchConditions(ObjectUtils.convertSortParams(request.getSearchConditions(), sortMap));

        var data = eventRegistrationRepository.searchByTermAndCondition(request, pageable);
        return PageUtils.toPage(data);
    }


    public void handleSaveEventAttendee(String apiLink) {
        if (StringUtils.isBlank(apiLink)) {
            return;
        }
        ResponseEventRegistration eventRegister = eventbriteService.getEventAttendeeByUrl(apiLink);

        if (Objects.isNull(eventRegister) || Objects.isNull(eventRegister.getExternalEventId())) {
            return;
        }

        var calendarEvents = calendarEventRepository.findAllByExternalEventIdInAndStatusNot(List.of(eventRegister.getExternalEventId()), EventStatusEnum.DELETED);
        if (CollectionUtils.isEmpty(calendarEvents)) {
            return;
        }
        var calendarEvent = calendarEvents.stream().findFirst().get();
        eventRegister.setCalendarEventId(calendarEvent.getId());
        removeOrUpdateAttendee(List.of(eventRegister));
    }

    public void removeOrUpdateAttendee(List<ResponseEventRegistration> externalAttendees) {
        List<String> newExternalAttendeeIds = externalAttendees
            .stream()
            .map(ResponseEventRegistration::getExternalAttendeeId)
            .toList();

        List<String> emailBOs = externalAttendees
            .stream()
            .map(ResponseEventRegistration::getEmailBO)
            .toList();

        List<EventRegistration> currentEvents = eventRegistrationRepository.findByExternalAttendeeIdIn(newExternalAttendeeIds);
        var currentEventMap = ObjectUtils.convertToMap(currentEvents, EventRegistration::getExternalAttendeeId);

        var emailToIdMap = businessOwnerRepository.findAllByEmails(emailBOs).stream()
            .collect(Collectors.toMap(IResponseBusinessOwnerEmail::getEmail, IResponseBusinessOwnerEmail::getId));

        List<EventRegistration> eventRegistrationsToSave = new ArrayList<>();
        for (ResponseEventRegistration externalEvent : externalAttendees) {
            var eventRegistration = mapToCalendarHuubEvent(externalEvent, currentEventMap, emailToIdMap);
            if (Objects.nonNull(eventRegistration)) {
                eventRegistrationsToSave.add(eventRegistration);
            }
        }
        eventRegistrationRepository.saveAll(eventRegistrationsToSave);

    }

    @Async
    public void handleSaveEventRegister() {
        List<CalendarEvent> events = calendarEventRepository.findAllByIsHuubEventAndStatusNot(true, EventStatusEnum.DELETED);

        if (events.isEmpty()) {
            return;
        }
        List<ResponseEventRegistration> eventRegistrations = new ArrayList<>();
        events.forEach(calendarEvent -> eventRegistrations.addAll(eventbriteService.getListEventAttendeeByUrl(calendarEvent)));
        removeOrUpdateAttendee(eventRegistrations);

    }

    private EventRegistration mapToCalendarHuubEvent(ResponseEventRegistration event, Map<String, EventRegistration> currentEventMap, Map<String, UUID> emailToIdMap) {
        if (Objects.isNull(event)
            || Objects.isNull(event.getExternalAttendeeId())
            || StringUtils.isBlank(event.getExternalEventId())
            || StringUtils.isBlank(event.getEmailBO())
        ) {
            return null;
        }
        var eventRegistration = currentEventMap.get(event.getExternalAttendeeId());
        var businessOwnerId = emailToIdMap.get(event.getEmailBO());
        if (Objects.isNull(businessOwnerId)) {
            return null;
        }

        if (!ObjectUtils.isEmpty(eventRegistration)) {
            eventRegistrationMapper.partialEntity(eventRegistration, event);
            return eventRegistration;
        }
        return eventRegistrationMapper.toEntity(event, businessOwnerId);
    }
}
