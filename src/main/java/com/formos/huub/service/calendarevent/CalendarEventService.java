package com.formos.huub.service.calendarevent;

import com.formos.huub.config.Constants;
import com.formos.huub.domain.entity.CalendarEvent;
import com.formos.huub.domain.entity.CalendarIntegration;
import com.formos.huub.domain.entity.Portal;
import com.formos.huub.domain.enums.*;
import com.formos.huub.domain.request.calendarevent.RequestCalendarEventLink;
import com.formos.huub.domain.request.calendarevent.RequestCreateEvent;
import com.formos.huub.domain.request.calendarevent.RequestPortalCalendarEventSetting;
import com.formos.huub.domain.request.calendarevent.RequestUpdateEvent;
import com.formos.huub.domain.request.eventcalendar.RequestSearchEvents;
import com.formos.huub.domain.response.calendarevent.RequestSearchCalendarEvents;
import com.formos.huub.domain.response.calendarevent.ResponseCalendarEventDetail;
import com.formos.huub.domain.response.calendarevent.ResponseCalendarEventLink;
import com.formos.huub.domain.response.calendarevent.ResponseRecommendEvent;
import com.formos.huub.domain.response.calendarintegrate.ResponseCalendarEvent;
import com.formos.huub.domain.response.eventcalendar.ResponseListEventCalendar;
import com.formos.huub.domain.response.portals.ResponseNewlyFeature;
import com.formos.huub.domain.response.webhook.eventbrite.ResponseEventbritePayload;
import com.formos.huub.framework.constant.AppConstants;
import com.formos.huub.framework.context.PortalContextHolder;
import com.formos.huub.framework.exception.BadRequestException;
import com.formos.huub.framework.message.MessageHelper;
import com.formos.huub.framework.message.model.Message;
import com.formos.huub.framework.properties.ScheduleProperties;
import com.formos.huub.framework.utils.ObjectUtils;
import com.formos.huub.framework.utils.PageUtils;
import com.formos.huub.framework.utils.StringUtils;
import com.formos.huub.mapper.calendarintegration.CalendarEventMapper;
import com.formos.huub.mapper.calendarintegration.CalendarIntegrationMapper;
import com.formos.huub.repository.CalendarEventRepository;
import com.formos.huub.repository.CalendarIntegrationRepository;
import com.formos.huub.repository.FeatureRepository;
import com.formos.huub.repository.PortalRepository;
import com.formos.huub.security.SecurityUtils;
import com.formos.huub.service.externalcalendar.EventbriteService;
import com.formos.huub.service.externalcalendar.ICalService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.formos.huub.domain.constant.BusinessConstant.NUMBER_0;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
@Slf4j
@EnableAsync(proxyTargetClass = true)
public class CalendarEventService {

    CalendarEventRepository calendarEventRepository;
    FeatureRepository featureRepository;

    CalendarIntegrationRepository calendarIntegrationRepository;

    CalendarIntegrationMapper calendarIntegrationMapper;

    CalendarEventMapper calendarEventMapper;

    PortalRepository portalRepository;

    ICalService iCalService;

    EventbriteService eventbriteService;

    ScheduleProperties scheduleProperties;

    /**
     * search Community Partners
     *
     * @param request RequestSearchCommunityPartner
     * @return Map<String, Object>
     */
    public Map<String, Object> searchPortalCalendarEvents(RequestSearchCalendarEvents request, String timezone) {
        var sort = !ObjectUtils.isEmpty(request.getSort()) ? request.getSort() : "ce.created_date,desc";
        var pageable = PageRequest.of(request.getPage(), request.getSize(), PageUtils.createSort(sort));
        if (Objects.isNull(request.getPortalId())) {
            request.setPortalId(PortalContextHolder.getPortalId());
        }
        processSearchConditions(request, timezone);
        var data = calendarEventRepository.searchByTermAndCondition(request, pageable);
        return PageUtils.toPage(data);
    }


    public List<ResponseListEventCalendar> getEventRelated(UUID eventId) {
        var event = getCalendarEvent(eventId);

        return calendarEventRepository.getEventRelatedByEvent(event.getId(), event.getOrganizerName())
            .stream().map(calendarEventMapper::toResponseList).toList();
    }

    public List<ResponseListEventCalendar> searchEventsByDate(RequestSearchEvents request, String timezone) {
        if (Objects.isNull(timezone)) {
            timezone = scheduleProperties.getTimezone();
        }
        if (Objects.isNull(request.getPortalId())) {
            request.setPortalId(PortalContextHolder.getPortalId());
        }
        return calendarEventRepository.searchCalendarEventListView(request, timezone)
            .stream().map(calendarEventMapper::toResponseList).toList();
    }

    /**
     * create calendar event for portal
     *
     * @param request RequestCreateEvent
     */
    public void createEvent(RequestCreateEvent request) {
        validateEventDate(request.getStartTime(), request.getEndTime());
        var event = calendarEventMapper.toEntityEvent(request);
        event.setInitBy(SecurityUtils.getCurrentUserLogin().orElse(null));
        var portals = getListPortals(request.getPortalIds());
        event.setPortals(portals);
        calendarEventRepository.save(event);
    }

    public ResponseCalendarEventDetail getDetailEvent(UUID eventId) {
        var event = getCalendarEvent(eventId);
        var portalIds = event.getPortals().stream().map(Portal::getId).toList();
        var response = calendarEventMapper.toResponseEvent(event);
        response.setPortalIds(portalIds);
        return response;
    }

    public void deleteEventById(UUID eventId) {
        var event = getCalendarEvent(eventId);
        event.setStatus(EventStatusEnum.DELETED);
        calendarEventRepository.save(event);
    }

    /**
     * update calendar event by id
     *
     * @param eventId UUID
     * @param request RequestUpdateEvent
     */
    public void updateEvent(UUID eventId, RequestUpdateEvent request) {
        validateEventDate(request.getStartTime(), request.getEndTime());
        var event = getCalendarEvent(eventId);
        calendarEventMapper.partialEntity(event, request);
        var portals = getListPortals(request.getPortalIds());
        event.setPortals(portals);
        calendarEventRepository.save(event);
    }

    /**
     * get Calendar Event Setting For Portal by portal ID
     *
     * @param portalId UUID
     * @return List<ResponseCalendarEventLink> response list
     */
    public List<ResponseCalendarEventLink> getCalendarEventSettingForPortal(UUID portalId) {
        return calendarIntegrationRepository
            .findAllByPortalIdAndIntegrateBy(portalId, IntegrateByEnum.PORTAL)
            .stream()
            .map(calendarIntegrationMapper::toResponseSetting)
            .sorted((Comparator.comparing(ResponseCalendarEventLink::getPriorityOrder)))
            .toList();
    }

    /**
     * Save Calendar Event Settings
     *
     * @param request RequestPortalCalendarEventSetting
     */
    public void saveCalendarEventSettings(RequestPortalCalendarEventSetting request) {
        validateCalendarEventSettingsInPortal(request);
        saveCalendarEvent(request, IntegrateByEnum.PORTAL);
    }

    public ResponseRecommendEvent getRecommendEvent(UUID portalId) {
        Pageable topOne = PageRequest.of(0, 1);
        var latestEvents = calendarEventRepository.findLatestByPortal(portalId, Instant.now(), topOne);
        if (latestEvents.isEmpty()) {
            return null;
        }
        var latest = latestEvents.get(NUMBER_0);
        return toResponseBaseEvents(latest);
    }

    public List<ResponseNewlyFeature> getNewlyEvents(UUID portalId, Integer size) {
        Pageable topOne = PageRequest.of(0, size);
        return calendarEventRepository.findLatestByPortal(portalId, Instant.now(), topOne).stream().map(this::toResponseNewlyEvents).toList();
    }

    private ResponseNewlyFeature toResponseNewlyEvents(CalendarEvent event) {
        return ResponseNewlyFeature.builder()
            .id(event.getId())
            .title(event.getSubject())
            .imageUrl(event.getImageUrl())
            .description(event.getDescription())
            .build();
    }

    private ResponseRecommendEvent toResponseBaseEvents(CalendarEvent event) {
        return ResponseRecommendEvent.builder()
            .id(event.getId())
            .title(event.getSubject())
            .imageUrl(event.getImageUrl())
            .startDate(event.getStartTime())
            .endDate(event.getEndTime())
            .build();
    }


    private void processSearchConditions(RequestSearchCalendarEvents request, String timezone) {
        HashMap<String, String> sortMap = new HashMap<>();
        sortMap.put("ce.startTime", "ce.start_time");
        sortMap.put(
            "createdBy",
            "(CASE WHEN ce.init_by = 'iCal' OR ce.init_by = 'Eventbrite' THEN ce.init_by ELSE u.normalized_full_name END)"
        );
        var searchConditions = Objects.requireNonNull(ObjectUtils.convertSortParams(request.getSearchConditions(), sortMap))
            .stream()
            .peek(ele -> {
                if (FilterTypeEnum.DATE.equals(ele.getFilterType())) {
                    ele.setTimezone(timezone);
                }
            })
            .toList();
        request.setRegexHtml(Constants.REMOVE_HTML_REGEX);
        request.setSearchConditions(searchConditions);
        request.setTimezone(timezone);
    }

    /**
     * Validate CalendarEvent Settings In Portal
     *
     * @param request RequestPortalCalendarEventSetting
     */
    private void validateCalendarEventSettingsInPortal(RequestPortalCalendarEventSetting request) {
        List<RequestCalendarEventLink> calendarEventSettings = request.getCalendarEventSettings();
        if (ObjectUtils.isEmpty(calendarEventSettings)) {
            return;
        }
        for (RequestCalendarEventLink eventLink : calendarEventSettings) {
            calendarIntegrationRepository.findByUrlAndPortalId(eventLink.getUrl(), request.getPortalId())
                .filter(existingEvent -> !existingEvent.getId().equals(eventLink.getId()))
                .ifPresent(duplicateEvent -> {
                    String calendarTypeName = duplicateEvent.getCalendarType().getName();
                    throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0048, calendarTypeName));
                });
        }
    }

    /**
     * Save CommunityPartner Events To Portal
     *
     * @param request RequestPortalCalendarEventSetting
     */
    public void saveCommunityPartnerEventsToPortal(RequestPortalCalendarEventSetting request) {
        saveCalendarEvent(request, IntegrateByEnum.COMMUNITY_PARTNER);
    }

    /**
     * Save Calendar Event
     *
     * @param request RequestPortalCalendarEventSetting object
     */
    private void saveCalendarEvent(RequestPortalCalendarEventSetting request, IntegrateByEnum integrateBy) {
        UUID entryId = integrateBy.equals(IntegrateByEnum.PORTAL) ? request.getPortalId() : request.getCommunityPartnerId();
        request.setIntegrateBy(integrateBy);
        var portal = getPortal(request.getPortalId());
        List<CalendarIntegration> currentCalendarIntegrations;
        if (integrateBy.equals(IntegrateByEnum.PORTAL)) {
            currentCalendarIntegrations = calendarIntegrationRepository.findAllByPortalIdAndIntegrateBy(portal.getId(), integrateBy);
        } else {
            currentCalendarIntegrations = calendarIntegrationRepository.findAllByCalendarId(String.join("|", request.getPortalId().toString(), entryId.toString()));
        }

        if (ObjectUtils.isEmpty(request.getCalendarEventSettings())) {
            handleArchivedAndDeleteCalendarEvents(currentCalendarIntegrations);
            return;
        }

        validateEventSetting(request.getCalendarEventSettings());
        var currentCalendarIntegrationMap = ObjectUtils.convertToMap(currentCalendarIntegrations, calendar
            -> String.join("|", request.getPortalId().toString(), entryId.toString(), integrateBy.getValue(), calendar.getUrl()));
        var calendarIntegrationDeletes = new ArrayList<>(currentCalendarIntegrations);
        var calendarIntegrationSaves = new ArrayList<CalendarIntegration>();

        processCalendarEventSettings(request, portal, currentCalendarIntegrationMap, calendarIntegrationSaves, calendarIntegrationDeletes);
        List<CalendarIntegration> savedCalendarIntegrations = calendarIntegrationRepository.saveAll(calendarIntegrationSaves);

        savedCalendarIntegrations.forEach(this::handleSaveEvent);
        handleArchivedAndDeleteCalendarEvents(calendarIntegrationDeletes);
    }

    /**
     * handle Archived And Delete Calendar Events when removed calendar integrations
     *
     * @param currentCalendarIntegrations List<CalendarIntegration>
     */
    private void handleArchivedAndDeleteCalendarEvents(List<CalendarIntegration> currentCalendarIntegrations) {
        handelArchivedCalendarEvent(currentCalendarIntegrations);
        calendarIntegrationRepository.deleteAll(currentCalendarIntegrations);
    }

    /**
     * process Calendar Event Settings update object
     *
     * @param request               RequestPortalCalendarEventSetting
     * @param portal                Portal
     * @param currentIntegrationMap Map<String, CalendarIntegration>
     * @param saves                 List<CalendarIntegration> saves
     * @param deletes               List<CalendarIntegration>
     */
    private void processCalendarEventSettings(
        RequestPortalCalendarEventSetting request,
        Portal portal,
        Map<String, CalendarIntegration> currentIntegrationMap,
        List<CalendarIntegration> saves,
        List<CalendarIntegration> deletes
    ) {
        int priorityOrder = 0;
        for (RequestCalendarEventLink eventSetting : request.getCalendarEventSettings()) {
            var calendarIntegration = updateOrCreateCalendarIntegration(
                portal,
                eventSetting,
                currentIntegrationMap,
                request.getIntegrateBy(),
                request.getCommunityPartnerId()
            );
            if (Objects.nonNull(calendarIntegration.getId())) {
                deletes.remove(calendarIntegration);
            }
            calendarIntegration.setPriorityOrder(priorityOrder++);
            saves.add(calendarIntegration);
        }
    }

    /**
     * update Or Create Calendar Integration
     *
     * @param portal     Portal
     * @param request    RequestCalendarEventLink
     * @param currentMap Map<String, CalendarIntegration>
     * @return CalendarIntegration
     */
    private CalendarIntegration updateOrCreateCalendarIntegration(
        Portal portal,
        RequestCalendarEventLink request,
        Map<String, CalendarIntegration> currentMap,
        IntegrateByEnum integrateBy,
        UUID communityPartnerId
    ) {
        UUID entryId = integrateBy.equals(IntegrateByEnum.PORTAL) ? portal.getId() : communityPartnerId;
        String calendarId = String.join("|", portal.getId().toString(), entryId.toString(), integrateBy.getValue(), request.getUrl());
        // Get existing CalendarIntegration or create a new one if it doesn't exist
        CalendarIntegration calendarIntegration = currentMap.getOrDefault(
            calendarId,
            CalendarIntegration.builder().portal(portal).build()
        );

        // Update fields regardless of whether it's a new or existing entry
        calendarIntegration.setCalendarType(request.getCalendarType());
        calendarIntegration.setUrl(request.getUrl());
        calendarIntegration.setCalendarStatus(CalendarStatusEnum.ACTIVE);
        calendarIntegration.setIntegrateBy(integrateBy);
        calendarIntegration.setSyncEventStatus(SyncEventStatusEnum.SUCCESS);
        calendarIntegration.setRetryCount(0);
        calendarIntegration.setLastSync(Instant.now());
        calendarIntegration.setCalendarId(calendarId);
        return calendarIntegration;
    }

    /**
     * Handle Save Calendar Event from event iCal or eventbrite link
     *
     * @param calendarIntegration CalendarIntegration
     */
    @Async
    public void handleSaveEvent(CalendarIntegration calendarIntegration) {
        String portalId = Optional.ofNullable(calendarIntegration.getPortal().getId()).map(Object::toString).orElse(StringUtils.EMPTY);

        List<ResponseCalendarEvent> events = getEvents(calendarIntegration);

        if (!events.isEmpty()) {
            events = events
                .stream()
                .peek(
                    event ->
                        event.setExternalEventId(
                            String.join("|", portalId, event.getExternalEventId(), calendarIntegration.getIntegrateBy().getValue())
                        )
                )
                .toList();

            removeOrUpdateEvents(calendarIntegration, events);
        }
    }

    @Async
    public void handleSaveHuubEvent() {
        List<ResponseCalendarEvent> events = eventbriteService.getHuubEventbriteEvents();

        if (events.isEmpty()) {
            return;
        }
        removeOrUpdateHuubEvents(events);
    }

    public void handleSaveEventbrite(String apiLink) {
        if (StringUtils.isBlank(apiLink)) {
            return;
        }
        ResponseCalendarEvent event = eventbriteService.getEventByUrl(apiLink);

        if (Objects.isNull(event)
            || Objects.isNull(event.getEventbriteStatus())
            || (!AppConstants.EVENT_LIVE.equals(event.getEventbriteStatus())
            && !AppConstants.EVENT_STARTED.equals(event.getEventbriteStatus()))) {
            return;
        }

        removeOrUpdateHuubEvents(List.of(event));
    }

    public void handleCancelEvent(String apiLink) {
        if (StringUtils.isBlank(apiLink)) {
            return;
        }
        ResponseCalendarEvent event = eventbriteService.getEventByUrl(apiLink);

        if (Objects.isNull(event) || Objects.isNull(event.getExternalEventId())) {
            return;
        }
        updateDeletedEvents(Set.of(event.getExternalEventId()));
    }

    /**
     * Get Events to sync
     *
     * @param integration CalendarIntegration
     * @return List<ResponseCalendarEvent>
     */
    private List<ResponseCalendarEvent> getEvents(CalendarIntegration integration) {
        String clientUrl = integration.getUrl();

        if (CalendarTypeEnum.ICALENDAR.equals(integration.getCalendarType())) {
            return iCalService.getEventsFromUrl(clientUrl, integration);
        }

        return eventbriteService.processEventBrite(clientUrl);

    }

    /**
     * remove Or Update Events from external events
     *
     * @param calendarIntegration CalendarIntegration
     * @param externalEvents      List<ResponseCalendarEvent>
     */
    //    private void removeOrUpdateEvents_old(CalendarIntegration calendarIntegration, List<ResponseCalendarEvent> externalEvents) {
    //        List<String> newExternalIds = ObjectUtils.toListString(externalEvents, ResponseCalendarEvent::getExternalEventId);
    //
    //        List<CalendarEvent> currentEvents = calendarEventRepository.getAllByCalendarIntegrationIdOrExternalIds(calendarIntegration.getId(), newExternalIds);
    //
    //        List<String> currentExternalIds = ObjectUtils.toListString(currentEvents, CalendarEvent::getExternalEventId);
    //
    //        var diff = ObjectUtils.getDifferenceList(currentExternalIds, newExternalIds);
    //        List<String> currentChecksums = currentEvents.stream().map(CalendarEvent::getChecksum).toList();
    //
    //        Map<String, CalendarEvent> currentEventMap = ObjectUtils.convertToMap(currentEvents, CalendarEvent::getExternalEventId);
    //        List<CalendarEvent> calendarEvents = externalEvents.stream()
    //            .map(event -> mapToCalendarEvent(event, currentEventMap, calendarIntegration))
    //            .toList();
    //
    //        List<CalendarEvent> saveCalendarEvents = calendarEvents.stream()
    //                .filter(event -> !currentChecksums.contains(event.getChecksum()))
    //                    .toList();
    //
    //        calendarEventRepository.saveAll(saveCalendarEvents);
    //        updateStatusEventByExternalEventIds(diff.getRemoved());
    //    }
    private void removeOrUpdateEvents(CalendarIntegration calendarIntegration, List<ResponseCalendarEvent> externalEvents) {
        List<String> newExternalIds = externalEvents
            .stream()
            .map(ResponseCalendarEvent::getExternalEventId)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        List<CalendarEvent> currentEvents = calendarEventRepository.getAllByCalendarIntegrationIdOrExternalIds(
            calendarIntegration.getId(),
            newExternalIds
        );

        Map<String, CalendarEvent> currentEventMap = currentEvents
            .stream()
            .collect(Collectors.toMap(CalendarEvent::getExternalEventId, Function.identity(), (e1, e2) -> e1));

        Set<String> currentExternalIds = currentEventMap.keySet();
        Set<String> removedExternalIds = new HashSet<>(currentExternalIds);
        newExternalIds.forEach(removedExternalIds::remove);

        List<CalendarEvent> eventsToSave = new ArrayList<>();

        for (ResponseCalendarEvent externalEvent : externalEvents) {
            CalendarEvent calendarEvent = mapToCalendarEvent(externalEvent, currentEventMap, calendarIntegration);
            String existingEventChecksum = Optional.ofNullable(currentEventMap.get(externalEvent.getExternalEventId()))
                .map(CalendarEvent::getChecksum)
                .orElse(null);

            if (existingEventChecksum == null || !existingEventChecksum.equals(calendarEvent.getChecksum())) {
                eventsToSave.add(calendarEvent);
            }
        }

        updateDeletedEvents(removedExternalIds);
        calendarEventRepository.saveAll(eventsToSave);
    }

    private void removeOrUpdateHuubEvents(List<ResponseCalendarEvent> externalEvents) {
        List<String> newExternalIds = externalEvents
            .stream()
            .map(ResponseCalendarEvent::getExternalEventId)
            .toList();

        List<CalendarEvent> currentEvents = calendarEventRepository.findAllByExternalEventIdInAndStatusNot(newExternalIds, EventStatusEnum.DELETED);

        var currentEventMap = ObjectUtils.convertToMap(currentEvents, CalendarEvent::getExternalEventId);
        var eventChecksums = currentEvents.stream().map(CalendarEvent::getChecksum).collect(Collectors.toSet());

        List<CalendarEvent> eventsToSave = externalEvents.stream()
            .map(externalEvent -> mapToCalendarHuubEvent(externalEvent, currentEventMap))
            .filter(calendarEvent -> !eventChecksums.contains(calendarEvent.getChecksum()))
            .toList();
        calendarEventRepository.saveAll(eventsToSave);
    }

    /**
     * update Deleted Events
     *
     * @param externalIds Set<String>
     */
    private void updateDeletedEvents(Set<String> externalIds) {
        if (!externalIds.isEmpty()) {
            calendarEventRepository.updateStatusForDeletedEvents(externalIds, EventStatusEnum.DELETED);
        }
    }

    /**
     * handel Archived Calendar Event when calendar integrations removed.
     *
     * @param calendarIntegrations List<CalendarIntegration>
     */
    private void handelArchivedCalendarEvent(List<CalendarIntegration> calendarIntegrations) {
        var calendarIntegrationIds = calendarIntegrations.stream().map(CalendarIntegration::getId).toList();
        var events = calendarEventRepository
            .getAllByCalendarIntegrationIdIn(calendarIntegrationIds)
            .stream()
            .peek(ele -> ele.setStatus(EventStatusEnum.ARCHIVED))
            .toList();
        calendarEventRepository.saveAll(events);
        calendarIntegrationRepository.deleteAll(calendarIntegrations);
    }

    /**
     * update Status Event By External Event Ids
     *
     * @param externalIds List<String>
     */
    private void updateStatusEventByExternalEventIds(List<String> externalIds) {
        var events = calendarEventRepository
            .findAllByExternalEventIdIn(externalIds)
            .stream()
            .peek(ele -> ele.setStatus(EventStatusEnum.DELETED))
            .toList();
        calendarEventRepository.saveAll(events);
    }

    /**
     * map  Object CalendarEvent to save
     *
     * @param responseEvent     ResponseCalendarEvent
     * @param currentEventMap   Map<String, CalendarEvent>
     * @param calendarIntegrate CalendarIntegration
     * @return CalendarEvent
     */
    private CalendarEvent mapToCalendarEvent(
        ResponseCalendarEvent responseEvent,
        Map<String, CalendarEvent> currentEventMap,
        CalendarIntegration calendarIntegrate
    ) {
        CalendarEvent existingEvent = currentEventMap.get(responseEvent.getExternalEventId());
        if (ObjectUtils.isEmpty(existingEvent)) {
            existingEvent = calendarEventMapper.toEntity(responseEvent);
            existingEvent.setPortals(Set.of(calendarIntegrate.getPortal()));
            existingEvent.setExternalEventId(responseEvent.getExternalEventId());
            existingEvent.setInitBy(calendarIntegrate.getCalendarType().getName());
            existingEvent.setWebsite(responseEvent.getEventUrl());
        } else {
            calendarEventMapper.partialEntity(existingEvent, responseEvent);
        }
        if (!ObjectUtils.isEmpty(responseEvent.getTimezone())) {
            existingEvent.setTimezone(responseEvent.getTimezone());
        }
        existingEvent.setStatus(getStatusEvent(existingEvent.getStatus()));
        existingEvent.setCalendarIntegration(calendarIntegrate);
        existingEvent.setChecksum(StringUtils.generateChecksum(responseEvent.toString()));
        return existingEvent;
    }

    /**
     * map  Object CalendarEvent to save
     *
     * @param responseEvent     ResponseCalendarEvent
     * @param currentEventMap   Map<String, CalendarEvent>
     * @return CalendarEvent
     */
    private CalendarEvent mapToCalendarHuubEvent(
        ResponseCalendarEvent responseEvent,
        Map<String, CalendarEvent> currentEventMap
    ) {
        CalendarEvent calendarEvent = currentEventMap.get(responseEvent.getExternalEventId());
        val portalIds = Set.copyOf(featureRepository.getAllPortalIdsEnableEventCalendar(FeatureCodeEnum.PORTALS_CALENDAR_AND_EVENTS));
        if (ObjectUtils.isEmpty(calendarEvent)) {
            calendarEvent = calendarEventMapper.toEntity(responseEvent);
            calendarEvent.setExternalEventId(responseEvent.getExternalEventId());
            calendarEvent.setInitBy("System");
            calendarEvent.setWebsite(responseEvent.getEventUrl());
            calendarEvent.setPortals(portalIds);
        } else {
            calendarEventMapper.partialEntity(calendarEvent, responseEvent);
        }
        if (!ObjectUtils.isEmpty(responseEvent.getTimezone())) {
            calendarEvent.setTimezone(responseEvent.getTimezone());
        }
        calendarEvent.setStatus(getStatusEvent(calendarEvent.getStatus()));
        calendarEvent.setHuubEvent(true);
        calendarEvent.setChecksum(StringUtils.generateChecksum(responseEvent.toString()));
        return calendarEvent;
    }

    /**
     * get Status event
     *
     * @param currentStatus EventStatusEnum
     * @return EventStatusEnum
     */
    private EventStatusEnum getStatusEvent(EventStatusEnum currentStatus) {
        if (Objects.nonNull(currentStatus) && Objects.equals(EventStatusEnum.DELETED, currentStatus)) {
            return currentStatus;
        }
        return EventStatusEnum.PUBLISHED;
    }

    /**
     * validate Event Setting from request
     *
     * @param calendarEventSettings List<RequestCalendarEventLink>
     */
    public void validateEventSetting(List<RequestCalendarEventLink> calendarEventSettings) {
        var urls = calendarEventSettings.stream().map(RequestCalendarEventLink::getUrl).toList();
        if (ObjectUtils.hasDuplicate(urls)) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0017, "Event Setting URL"));
        }
        calendarEventSettings.forEach(ele -> {
            if (CalendarTypeEnum.ICALENDAR.equals(ele.getCalendarType()) && iCalService.isValidICalUrl(ele.getUrl())) {
                throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0015, "ICal URL"));
            } else if (
                CalendarTypeEnum.EVENTBRITE.equals(ele.getCalendarType()) &&
                    EventbriteUrlEnum.INVALID.equals(eventbriteService.checkEventbriteUrl(ele.getUrl()))
            ) {
                throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0015, "Eventbrite URL"));
            }
        });
    }

    /**
     * validate Event Date
     *
     * @param start Instant
     * @param end   Instant
     */
    private void validateEventDate(Instant start, Instant end) {
        if (start.isAfter(end)) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0015, "Event time"));
        }
    }

    /**
     * get Calendar Event by ID
     *
     * @param id UUID
     * @return CalendarEvent Object Entity
     */
    private CalendarEvent getCalendarEvent(UUID id) {
        return calendarEventRepository
            .findById(id)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Calendar Event")));
    }

    /**
     * get Portal by ID
     *
     * @param id UUID
     * @return Portal Entity
     */
    private Portal getPortal(UUID id) {
        return portalRepository
            .findById(id)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Portal")));
    }

    /**
     * get List Portals
     *
     * @param portalIds List<UUID>
     * @return Set<Portal>
     */
    private Set<Portal> getListPortals(List<UUID> portalIds) {
        if (ObjectUtils.isEmpty(portalIds)) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0016, "Portals"));
        }
        var portals = portalRepository.findAllById(portalIds);
        if (portals.size() != portalIds.size()) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Portals"));
        }
        return new HashSet<>(portals);
    }
}
