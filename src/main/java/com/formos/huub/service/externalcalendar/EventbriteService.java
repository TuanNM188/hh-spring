package com.formos.huub.service.externalcalendar;

import com.formos.huub.domain.entity.CalendarEvent;
import com.formos.huub.domain.enums.EventRegistrationStatusEnum;
import com.formos.huub.domain.enums.EventbriteUrlEnum;
import com.formos.huub.domain.response.calendarintegrate.*;
import com.formos.huub.domain.response.eventregistration.ResponseEventRegistration;
import com.formos.huub.domain.response.eventregistration.ResponseEventbriteAttendee;
import com.formos.huub.domain.response.eventregistration.ResponseListEventbriteAttendee;
import com.formos.huub.framework.properties.EventbriteProperties;
import com.formos.huub.framework.utils.ObjectUtils;
import com.formos.huub.framework.utils.StringUtils;
import com.formos.huub.service.resttemplate.RestTemplateService;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
@Slf4j
public class EventbriteService {

    RestTemplateService restTemplateService;

    EventbriteProperties eventbriteProperties;

    private static final String EVENTBRITE_URL_REGEX = "^https:\\/\\/www\\.eventbrite\\.com\\/e\\/[a-zA-Z0-9-]+-tickets-(\\d+)(\\?.*)?$";
    private static final String ORGANIZER_EVENTBRITE_URL_REGEX = "^https:\\/\\/www\\.eventbrite\\.com\\/o\\/(?:[\\w-]+-)?(\\d+)$";
    private static final String EVENT_REGEX = ".*/e/.+-tickets-(\\d+).*";
    private static final String ORGANIZER_REGEX = ".*/o/(?:.*-)?(\\d+)$";

    private static final String LOG_START_FETCH = "Start fetch {} from Api at: {}";
    private static final String LOG_END_FETCH = "End fetch {} from Api at: {}";

    @Getter
    @Builder
    private static class ApiCallConfig<T, R> {

        private String url;
        private String resourceName;
        private Class<T> responseType;
        private Function<ResponseEntity<T>, R> responseHandler;
    }

    /**
     * Process the Eventbrite URL and fetch the events
     *
     * @param clientUrl Client URL
     * @return List of calendar events
     */
    public List<ResponseCalendarEvent> processEventBrite(String clientUrl) {
        EventbriteUrlEnum urlType = checkEventbriteUrl(clientUrl);

        String apiUrl = generateApiUrl(clientUrl, urlType);
        return fetchEvents(apiUrl, urlType);
    }

    public List<ResponseCalendarEvent> getHuubEventbriteEvents() {
        return getHuubOrganizations().stream()
            .map(organization -> String.format(eventbriteProperties.getOrganizationEventsUrl(), organization.getId()))
            .flatMap(apiUrl -> fetchEventbriteEventFromApi(apiUrl).stream())
            .map(this::mapToCalendarEvent)
            .collect(Collectors.toList());
    }

    public List<ResponseOrganization> getHuubOrganizations() {
        String apiUrl = eventbriteProperties.getOrganizationsUrl();
        return fetchOrganizationsFromApi(apiUrl);
    }

    public ResponseCalendarEvent getEventByUrl(String apiUrl) {
        if (Objects.isNull(apiUrl)) {
            return null;
        }
        return fetchEventPublicFromApi(apiUrl);
    }

    public List<ResponseEventRegistration> getListEventAttendeeByUrl(CalendarEvent calendarEvent) {
        if (Objects.isNull(calendarEvent)) {
            return null;
        }
        var apiUrl = String.format(eventbriteProperties.getEventAttendeesUrl(), calendarEvent.getExternalEventId());
        return fetchListEventAttendeeFromApi(apiUrl, calendarEvent.getId());
    }

    public ResponseEventRegistration getEventAttendeeByUrl(String apiUrl) {
        if (Objects.isNull(apiUrl)) {
            return null;
        }
        return fetchEventAttendeeFromApi(apiUrl);
    }

    /**
     * Check if the URL is an Eventbrite URL
     *
     * @param url URL
     * @return URL type
     */
    public EventbriteUrlEnum checkEventbriteUrl(String url) {
        Pattern organizerPattern = Pattern.compile(ORGANIZER_EVENTBRITE_URL_REGEX);
        Pattern eventPattern = Pattern.compile(EVENTBRITE_URL_REGEX);

        if (StringUtils.isEmpty(url)) {
            return EventbriteUrlEnum.INVALID;
        }

        Matcher eventMatcher = eventPattern.matcher(url);
        if (eventMatcher.matches()) {
            return EventbriteUrlEnum.SINGLE_EVENT;
        }

        Matcher organizerMatcher = organizerPattern.matcher(url);
        if (organizerMatcher.matches()) {
            return EventbriteUrlEnum.ORGANIZER;
        }

        return EventbriteUrlEnum.INVALID;
    }

    /**
     * Create the headers for the Eventbrite API request
     *
     * @param apiToken API token
     * @return Headers
     */
    private HttpHeaders createAuthHeaders(String apiToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiToken);
        return headers;
    }

    /**
     * Generate the API URL based on the client URL
     *
     * @param clientUrl Client URL
     * @param urlType   URL type
     * @return API URL
     */
    private String generateApiUrl(String clientUrl, EventbriteUrlEnum urlType) {
        switch (urlType) {
            case SINGLE_EVENT:
                String eventId = extractEventId(clientUrl);
                return String.format(eventbriteProperties.getSingleEventUrl(), eventId);
            case ORGANIZER:
                String organizerId = extractOrganizerId(clientUrl);
                return String.format(eventbriteProperties.getOrganizerEventsUrl(), organizerId);
            default:
                throw new IllegalArgumentException("Unsupported URL type");
        }
    }

    /**
     * Fetch the events from the Eventbrite API
     *
     * @param apiUrl  API URL
     * @param urlType URL type
     * @return List of calendar events
     */
    private List<ResponseCalendarEvent> fetchEvents(String apiUrl, EventbriteUrlEnum urlType) {
        return switch (urlType) {
            case SINGLE_EVENT -> fetchEventsFromApi(apiUrl, ResponseEventbriteEvent.class, true);
            case ORGANIZER -> fetchEventsFromApi(apiUrl, ResponseListEventbriteEvent.class, false);
            default -> Collections.emptyList();
        };
    }

    private ResponseCalendarEvent fetchEventPublicFromApi(String apiUrl) {
        return executeApiCall(
            ApiCallConfig.<ResponseEventbriteEvent, ResponseCalendarEvent>builder()
                .url(apiUrl)
                .resourceName("single event")
                .responseType(ResponseEventbriteEvent.class)
                .responseHandler(response -> Optional.ofNullable(response.getBody()).map(this::mapToCalendarEvent).orElse(null))
                .build()
        );
    }

    private List<ResponseEventRegistration> fetchListEventAttendeeFromApi(String apiUrl, UUID calendarEventId) {
        return fetchPaginatedResults(
            apiUrl,
            "event attendees",
            ResponseListEventbriteAttendee.class,
            page ->
                Optional.ofNullable(page.getAttendees())
                    .orElse(Collections.emptyList())
                    .stream()
                    .map(attendee -> mapToEventRegistration(attendee, calendarEventId))
                    .collect(Collectors.toList())
        );
    }

    private ResponseEventRegistration fetchEventAttendeeFromApi(String apiUrl) {
        return executeApiCall(
            ApiCallConfig.<ResponseEventbriteAttendee, ResponseEventRegistration>builder()
                .url(apiUrl)
                .resourceName("event attendee")
                .responseType(ResponseEventbriteAttendee.class)
                .responseHandler(
                    response -> Optional.ofNullable(response.getBody()).map(body -> mapToEventRegistration(body, null)).orElse(null)
                )
                .build()
        );
    }

    /**
     * Fetch the events from the Eventbrite API
     *
     * @param apiUrl        API URL
     * @param responseType  Response type
     * @param isSingleEvent Flag to indicate if the API response is for a single event
     * @param <T>           Response type
     * @return List of calendar events
     */
    private <T> List<ResponseCalendarEvent> fetchEventsFromApi(String apiUrl, Class<T> responseType, boolean isSingleEvent) {
        return executeApiCall(
            ApiCallConfig.<T, List<ResponseCalendarEvent>>builder()
                .url(apiUrl)
                .resourceName("events")
                .responseType(responseType)
                .responseHandler(response ->
                    Optional.ofNullable(response.getBody())
                        .map(body -> {
                            if (isSingleEvent) {
                                return List.of(mapToCalendarEvent((ResponseEventbriteEvent) body));
                            }
                            return ((ResponseListEventbriteEvent) body).getEvents()
                                .stream()
                                .map(this::mapToCalendarEvent)
                                .collect(Collectors.toList());
                        })
                        .orElse(Collections.emptyList()))
                .build()
        );
    }

    private List<ResponseEventbriteEvent> fetchEventbriteEventFromApi(String apiUrl) {
        return fetchPaginatedResults(apiUrl, "events", ResponseEventbriteEventPageable.class, ResponseEventbriteEventPageable::getEvents);
    }

    private List<ResponseOrganization> fetchOrganizationsFromApi(String apiUrl) {
        return fetchPaginatedResults(
            apiUrl,
            "organizations",
            ResponseOrganizationPageable.class,
            ResponseOrganizationPageable::getOrganizations
        );
    }


    private ResponseOrganizer getOrganizerById(String organizerId) throws RestClientException {
        String apiUrl = String.format(eventbriteProperties.getOrganizerUrl(), organizerId);
        return executeApiCall(
            ApiCallConfig.<ResponseOrganizer, ResponseOrganizer>builder()
                .url(apiUrl)
                .resourceName("Organizer")
                .responseType(ResponseOrganizer.class)
                .responseHandler(HttpEntity::getBody)
                .build()
        );
    }

    private ResponseVenueEvent getVenueById(String venueId) throws RestClientException {
        String apiUrl = String.format(eventbriteProperties.getVenueDetailUrl(), venueId);
        return executeApiCall(
            ApiCallConfig.<ResponseVenueEvent, ResponseVenueEvent>builder()
                .url(apiUrl)
                .resourceName("Venue")
                .responseType(ResponseVenueEvent.class)
                .responseHandler(HttpEntity::getBody)
                .build()
        );
    }

    /**
     * Map the Eventbrite event data to the calendar event data
     *
     * @param data Eventbrite event data
     * @return Calendar event data
     */
    private ResponseCalendarEvent mapToCalendarEvent(ResponseEventbriteEvent data) {
        ResponseCalendarEvent.ResponseCalendarEventBuilder eventBuilder = ResponseCalendarEvent.builder()
            .externalEventId(data.getId())
            .subject(data.getName().getText())
            .summary(data.getSummary())
            .description(data.getSummary())
            .startTime(data.getStart().getUtc())
            .endTime(data.getEnd().getUtc())
            .timezone(data.getStart().getTimezone())
            .eventbriteStatus(data.getStatus())
            .eventUrl(data.getUrl());
        if (Objects.nonNull(data.getLogo()) && Objects.nonNull(data.getLogo().getOriginal())) {
            eventBuilder.imageUrl(data.getLogo().getOriginal().getUrl());
        }

        if (!ObjectUtils.isEmpty(data.getOrganizerId())) {
            var organizer = getOrganizerById(data.getOrganizerId());
            if (Objects.nonNull(organizer)) {
                eventBuilder.organizerName(organizer.getName());
            }
        }

        if (!ObjectUtils.isEmpty(data.getVenueId())) {
            var venue = getVenueById(data.getVenueId());
            if (Objects.nonNull(venue) && Objects.nonNull(venue.getAddress())) {
                eventBuilder.location(venue.getAddress().getLocalizedAddressDisplay());
            }
        }

        return eventBuilder.build();
    }

    private ResponseEventRegistration mapToEventRegistration(ResponseEventbriteAttendee data, UUID calendarEventId) {
        return ResponseEventRegistration.builder()
            .externalAttendeeId(data.getId())
            .externalEventId(data.getEventId())
            .emailBO(data.getProfile().getEmail())
            .created(data.getCreated())
            .status(getStatusByName(data.getStatus()))
            .isCheckedIn(data.getCheckedIn())
            .isCancelled(data.getCancelled())
            .isRefunded(data.getRefunded())
            .calendarEventId(calendarEventId)
            .build();
    }

    private EventRegistrationStatusEnum getStatusByName(String statusName) {
        return Optional.ofNullable(statusName)
            .flatMap(name -> Arrays.stream(EventRegistrationStatusEnum.values()).filter(e -> e.getName().equals(name)).findFirst())
            .orElse(null);
    }

    /**
     * Extract the event ID from the Eventbrite URL
     *
     * @param url Eventbrite URL
     * @return Event ID
     */
    private String extractEventId(String url) {
        String eventId = null;

        // Regular expression to match Eventbrite URL and extract the event ID
        Pattern pattern = Pattern.compile(EVENT_REGEX);
        Matcher matcher = pattern.matcher(url);

        if (matcher.matches()) {
            eventId = matcher.group(1); // Group 1 is the event ID
        }

        return eventId;
    }

    /**
     * Extract the organizer ID from the Eventbrite URL
     *
     * @param url Eventbrite URL
     * @return Organizer ID
     */
    private String extractOrganizerId(String url) {
        String organizerId = null;
        Pattern pattern = Pattern.compile(ORGANIZER_REGEX);
        Matcher matcher = pattern.matcher(url);

        if (matcher.find()) {
            organizerId = matcher.group(1);
        }

        return organizerId;
    }

    private <T, R> R executeApiCall(ApiCallConfig<T, R> config) {
        if (config.getResourceName() != null) {
            log.info(LOG_START_FETCH, config.getResourceName(), Instant.now());
        }

        try {
            HttpHeaders headers = createAuthHeaders(eventbriteProperties.getApiToken());
            ResponseEntity<T> response = restTemplateService.sendGetRequest(config.getUrl(), headers, config.getResponseType());

            if (config.getResourceName() != null) {
                log.info(LOG_END_FETCH, config.getResourceName(), Instant.now());
            }

            if (!response.getStatusCode().equals(HttpStatus.OK)) {
                throw new RuntimeException(response.toString());
            }
            if (response.getBody() == null) {
                return null;
            }

            return config.getResponseHandler().apply(response);
        } catch (HttpClientErrorException e) {
            log.error("Client error when fetching Eventbrite {}: {}", config.getResourceName(), e.getStatusCode(), e);
            throw e;
        } catch (ResourceAccessException e) {
            log.error("Network error when fetching Eventbrite {}", config.getResourceName(), e);
            throw e;
        } catch (RestClientException e) {
            log.error("Error when fetching Eventbrite {}", config.getResourceName(), e);
            throw e;
        }
    }

    private <T, R> List<R> fetchPaginatedResults(
        String baseUrl,
        String resourceName,
        Class<T> responseClass,
        Function<T, List<R>> itemsExtractor
    ) {
        log.info("Start fetching paginated {} at: {}", resourceName, Instant.now());
        List<R> results = new ArrayList<>();
        String currentUrl = baseUrl;
        int pageCount = 0;

        try {
            while (true) {
                T page = executeApiCall(
                    ApiCallConfig.<T, T>builder()
                        .url(currentUrl)
                        .resourceName(resourceName)
                        .responseType(responseClass)
                        .responseHandler(HttpEntity::getBody)
                        .build()
                );

                if (page == null) {
                    break;
                }

                List<R> items = itemsExtractor.apply(page);
                if (items == null || items.isEmpty()) {
                    break;
                }

                results.addAll(items);

                ResponsePageable pagination = ((AbstractResponsePageable<?>) page).getPagination();
                if (pagination == null || pagination.getPageNumber() >= pagination.getPageCount() || !pagination.isHasMoreItems()) {
                    break;
                }

                currentUrl = baseUrl + "?continuation=" + pagination.getContinuation();
                pageCount++;
            }
        } finally {
            log.info("End fetching paginated {} at: {}. Total pages processed: {}", resourceName, Instant.now(), pageCount);
        }

        return results;
    }
}
