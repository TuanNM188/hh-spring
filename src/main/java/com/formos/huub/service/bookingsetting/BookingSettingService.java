package com.formos.huub.service.bookingsetting;

import com.formos.huub.domain.entity.*;
import com.formos.huub.domain.enums.CalendarStatusEnum;
import com.formos.huub.domain.enums.CalendarTypeEnum;
import com.formos.huub.domain.request.bookingsetting.RequestBookingSetting;
import com.formos.huub.domain.request.bookingsetting.RequestConnectCalendar;
import com.formos.huub.domain.request.bookingsetting.RequestTechnicalAdvisorAvailability;
import com.formos.huub.domain.response.bookingsetting.ResponseSetting;
import com.formos.huub.domain.response.calendarintegrate.ResponseAttributeCalendar;
import com.formos.huub.domain.response.calendarintegrate.ResponseCalendarEvent;
import com.formos.huub.domain.response.calendarintegrate.ResponseCalendarExternal;
import com.formos.huub.framework.exception.BadRequestException;
import com.formos.huub.framework.exception.NotFoundException;
import com.formos.huub.framework.message.MessageHelper;
import com.formos.huub.framework.message.model.Message;
import com.formos.huub.framework.utils.ObjectUtils;
import com.formos.huub.framework.utils.StringUtils;
import com.formos.huub.mapper.bookingsetting.AvailabilityMapper;
import com.formos.huub.mapper.bookingsetting.BookingSettingMapper;
import com.formos.huub.mapper.calendarintegration.CalendarEventMapper;
import com.formos.huub.mapper.calendarintegration.CalendarIntegrationMapper;
import com.formos.huub.repository.*;
import com.formos.huub.service.externalcalendar.GoogleCalendarService;
import com.formos.huub.service.externalcalendar.ICalService;
import com.formos.huub.service.externalcalendar.MicrosoftCalendarService;
import com.google.gson.Gson;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingSettingService {

    BookingSettingRepository bookingSettingRepository;

    AvailabilityRepository availabilityRepository;

    TechnicalAdvisorRepository technicalAdvisorRepository;

    BookingSettingMapper bookingSettingMapper;

    AvailabilityMapper availabilityMapper;

    CalendarIntegrationRepository calendarIntegrationRepository;

    CalendarTokenRepository calendarTokenRepository;

    CalendarEventRepository calendarEventRepository;

    GoogleCalendarService googleCalendarService;

    MicrosoftCalendarService microsoftCalendarService;

    CalendarIntegrationMapper calendarIntegrationMapper;

    CalendarEventMapper calendarEventMapper;

    ICalService iCalService;

    UserRepository userRepository;

    /**
     * update Booking Setting
     *
     * @param request RequestBookingSetting
     */
    public void updateBookingSetting(RequestBookingSetting request) throws IOException, URISyntaxException {
        boolean allowBooking = request.isAllowBooking();
        User user = getUserByUserIdOrTechnicalAdvisorId(request.getUserId());
        BookingSetting bookingSetting = getOrCreateSetting(request);
        if (allowBooking) {
            validateBookingSetting(request, bookingSetting.getEarliestDate());
        }
        bookingSetting.setEarliestDate(request.getEarliestDate());
        user.setAllowBooking(allowBooking);
        bookingSetting.setUser(user);
        var savedSetting = bookingSettingRepository.saveAndFlush(bookingSetting);
        saveDataAvailabilitySetting(request.getAvailabilities(), savedSetting);
        handleCalendarIntegrate(request.getCalendarIntegrate(), savedSetting);
    }

    /**
     * Get Booking Setting Of User
     *
     * @param userOrTechnicalAdvisorId UUID
     * @return ResponseSetting
     */
    public ResponseSetting getBookingSettingOfUser(UUID userOrTechnicalAdvisorId) {
        User user = getUserByUserIdOrTechnicalAdvisorId(userOrTechnicalAdvisorId);

        return getSettingOfUser(user.getId());
    }

    /**
     * Get user by userId or technicalAdvisorId
     *
     * @param userOrTechnicalAdvisorId UUID
     * @return User
     */
    public User getUserByUserIdOrTechnicalAdvisorId(UUID userOrTechnicalAdvisorId) {
        return userRepository.findByUserIdOrTechnicalAdvisorId(userOrTechnicalAdvisorId)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "User")));
    }

    /**
     * get Setting Of User
     *
     * @param userId UUID
     * @return ResponseSetting
     */
    public ResponseSetting getSettingOfUser(UUID userId) {
        var settingOpt = bookingSettingRepository.findByUserId(userId);
        if (settingOpt.isEmpty()) {
            return new ResponseSetting(userId);
        }
        var setting = settingOpt.get();
        var response = bookingSettingMapper.toResponse(setting);
        var availabilities = availabilityMapper.toResponseList(setting.getAvailabilities().stream().toList());
        response.setAvailabilities(availabilities);
        response.setAllowBooking(setting.getUser().getAllowBooking());
        response.setUserId(setting.getUser().getId());
        var calendarIntegration = setting.getCalendarIntegration();
        getDataCalendarIntegration(response, calendarIntegration);
        return response;
    }

    private void getDataCalendarIntegration(ResponseSetting response, CalendarIntegration calendarIntegration) {
        if (Objects.isNull(calendarIntegration)) {
            return;
        }
        var attributes = extractAttribute(calendarIntegration.getAttributes());
        var calendarIntegrate = calendarIntegrationMapper.toResponse(calendarIntegration);
        calendarIntegrate.setEmail(attributes.getEmail());
        response.setCalendarIntegration(calendarIntegrate);
        if (!CalendarTypeEnum.ICALENDAR.equals(calendarIntegrate.getCalendarType())) {
            var accessToken = getAccessToken(calendarIntegration);
            var calendarList = getCalendarList(calendarIntegrate.getId(), calendarIntegrate.getCalendarType(), accessToken);
            if (ObjectUtils.isEmpty(calendarList)) {
                calendarList = attributes.getCalendarExternals();
            }
            response.setCalendarExternals(calendarList);
        }
    }

    /**
     * @param calendarIntegration CalendarIntegration
     * @return String
     */
    private String getAccessToken(CalendarIntegration calendarIntegration) {
        if (CalendarTypeEnum.GOOGLE.equals(calendarIntegration.getCalendarType())) {
            return googleCalendarService.getGoogleAccessToken(calendarIntegration.getId());
        }
        return microsoftCalendarService.getMicrosoftAccessToken(calendarIntegration.getId());
    }

    /**
     * extract Attribute
     *
     * @param attributesJson String
     * @return ResponseAttributeCalendar
     */
    private ResponseAttributeCalendar extractAttribute(String attributesJson) {
        if (ObjectUtils.isEmpty(attributesJson)) {
            return new ResponseAttributeCalendar();
        }
        var gson = new Gson();
        return gson.fromJson(attributesJson, ResponseAttributeCalendar.class);
    }

    /**
     * handle Calendar Integrate
     *
     * @param request        RequestConnectCalendar
     * @param bookingSetting BookingSetting
     */
    private void handleCalendarIntegrate(RequestConnectCalendar request, BookingSetting bookingSetting)
        throws IOException, URISyntaxException {
        if (Objects.isNull(request) || CalendarStatusEnum.DISCONNECT.equals(request.getCalendarStatus())) {
            disconnectCalendarIntegrate(bookingSetting.getId());
            return;
        }
        if (Objects.nonNull(request.getCalendarType())) {
            connectCalendarIntegrate(request, bookingSetting);
        }
    }

    /**
     * connect Calendar Integrate
     *
     * @param request        RequestConnectCalendar
     * @param bookingSetting BookingSetting
     */
    private void connectCalendarIntegrate(RequestConnectCalendar request, BookingSetting bookingSetting)
        throws IOException, URISyntaxException {
        var user = bookingSetting.getUser();
        // check the TA has previously connected the calendar
        var calendarIntegrationOpt = calendarIntegrationRepository.findByBookingSettingUserId(user.getId());
        CalendarIntegration calendarIntegrate;
        String oldCalendarRefIds = StringUtils.EMPTY;
        if (calendarIntegrationOpt.isPresent()) {
            calendarIntegrate = calendarIntegrationOpt.get();
            oldCalendarRefIds = calendarIntegrate.getCalendarRefId();
            calendarIntegrationMapper.partialEntity(calendarIntegrate, request);
        } else {
            calendarIntegrate = calendarIntegrationMapper.toEntity(request);
            calendarIntegrate.setBookingSetting(bookingSetting);
        }
        calendarIntegrate.setCalendarStatus(CalendarStatusEnum.ACTIVE);
        calendarIntegrate.setUrl(request.getCalLink());
        calendarIntegrate.setAttributes(buildAttributeCalendar(calendarIntegrate, request));
        calendarIntegrate = calendarIntegrationRepository.saveAndFlush(calendarIntegrate);

        saveToken(request, calendarIntegrate);
        syncExternalCalendarEventByCalendarId(calendarIntegrate, oldCalendarRefIds);
    }

    /**
     * @param calendarIntegrate CalendarIntegration
     * @param request           RequestConnectCalendar
     * @return String
     */
    private String buildAttributeCalendar(CalendarIntegration calendarIntegrate, RequestConnectCalendar request) {
        String accessToken = request.getIsNewToken() ? request.getAccessToken() : getAccessTokenCalendar(calendarIntegrate);
        var calendarList = getCalendarList(calendarIntegrate.getId(), calendarIntegrate.getCalendarType(), accessToken);
        var attributes = ResponseAttributeCalendar.builder().email(request.getEmail()).calendarExternals(calendarList);
        var gson = new Gson();
        return gson.toJson(attributes);
    }

    /**
     * save Token External calendar
     *
     * @param request             RequestConnectCalendar
     * @param calendarIntegration CalendarIntegration
     */
    private void saveToken(RequestConnectCalendar request, CalendarIntegration calendarIntegration) {
        if (Boolean.TRUE.equals(request.getIsNewToken()) && CalendarTypeEnum.GOOGLE.equals(request.getCalendarType())) {
            googleCalendarService.updateAccessToken(
                request.getAccessToken(),
                request.getRefreshToken(),
                request.getExpireDate(),
                calendarIntegration,
                request.getIsNewToken()
            );
        } else if (Boolean.TRUE.equals(request.getIsNewToken()) && CalendarTypeEnum.OUTLOOK.equals(request.getCalendarType())) {
            microsoftCalendarService.updateAccessToken(
                request.getAccessToken(),
                request.getRefreshToken(),
                request.getExpireDate(),
                calendarIntegration,
                request.getIsNewToken()
            );
        }
    }

    /**
     * get Calendar List of user connect calendar
     *
     * @param calendarIntegrateId UUID
     * @param calendarType        CalendarTypeEnum
     * @return List<ResponseCalendarExternal>
     */
    public List<ResponseCalendarExternal> getCalendarList(UUID calendarIntegrateId, CalendarTypeEnum calendarType, String accessToken) {
        if (calendarType == CalendarTypeEnum.GOOGLE && Objects.nonNull(accessToken)) {
            return googleCalendarService.getCalendarList(accessToken, calendarIntegrateId);
        } else if (calendarType == CalendarTypeEnum.OUTLOOK) {
            return microsoftCalendarService.getAllCalendarUser(calendarIntegrateId, accessToken);
        }
        return List.of();
    }

    /**
     * job Schedule Sync Event From Calendar Integrate
     */
    public void jobScheduleSyncEventFromCalendarIntegrate() {
        calendarIntegrationRepository
            .findAllByCalendarStatusAndCalendarTypeIn(CalendarStatusEnum.ACTIVE, List.of(CalendarTypeEnum.ICALENDAR))
            .forEach(ele -> {
                try {
                    syncExternalCalendarEventByCalendarId(ele, ele.getCalendarRefId());
                } catch (IOException | URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    /**
     * sync External Calendar Event By CalendarId
     *
     * @param calendarIntegrate CalendarIntegration
     * @param oldCalendarRefId  String
     */
    @Async
    public void syncExternalCalendarEventByCalendarId(CalendarIntegration calendarIntegrate, String oldCalendarRefId)
        throws IOException, URISyntaxException {
        // handle toggle OFF calendar
        handelToggleCalendar(calendarIntegrate.getCalendarRefId(), oldCalendarRefId);
        // handle toggle ON calendar
        List<ResponseCalendarEvent> events = getEventsFromExternalCalendar(calendarIntegrate);
        List<CalendarEvent> currentEvents = calendarEventRepository.getAllByCalendarIntegrationId(calendarIntegrate.getId());
        List<String> currentExternalIds = currentEvents
            .stream()
            .map(CalendarEvent::getExternalEventId)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        List<String> newExternalIds = events
            .stream()
            .map(ResponseCalendarEvent::getExternalEventId)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        var diff = ObjectUtils.getDifferenceList(currentExternalIds, newExternalIds);

        // Delete events removed from Google Calendar
        calendarEventRepository.deleteAllByExternalEventIdIn(diff.getRemoved());

        // Add or update events synced from Google Calendar
        Map<String, CalendarEvent> currentEventMap = currentEvents
            .stream()
            .collect(Collectors.toMap(CalendarEvent::getExternalEventId, Function.identity(), (existing, replacement) -> existing));
        List<CalendarEvent> calendarEvents = events
            .stream()
            .map(event -> mapToCalendarEvent(event, currentEventMap, calendarIntegrate))
            .collect(Collectors.toList());

        calendarEventRepository.saveAll(calendarEvents);
    }

    /**
     * get Access Token Calendar
     *
     * @param calendarIntegrate CalendarIntegration
     * @return String
     */
    private String getAccessTokenCalendar(CalendarIntegration calendarIntegrate) {
        if (CalendarTypeEnum.GOOGLE.equals(calendarIntegrate.getCalendarType())) {
            return googleCalendarService.getGoogleAccessToken(calendarIntegrate.getId());
        }
        if (CalendarTypeEnum.OUTLOOK.equals(calendarIntegrate.getCalendarType())) {
            return microsoftCalendarService.getMicrosoftAccessToken(calendarIntegrate.getId());
        }
        return StringUtils.EMPTY;
    }

    /**
     * handel Toggle Calendar
     *
     * @param newCalendarRefId String
     * @param oldCalendarRefId String
     */
    private void handelToggleCalendar(String newCalendarRefId, String oldCalendarRefId) {
        if (Objects.isNull(newCalendarRefId)) {
            newCalendarRefId = StringUtils.EMPTY;
        }
        if (Objects.isNull(oldCalendarRefId)) {
            oldCalendarRefId = StringUtils.EMPTY;
        }
        var newIds = Arrays.stream(newCalendarRefId.split(",")).toList();
        var oldIds = Arrays.stream(oldCalendarRefId.split(",")).toList();
        var diffRemoved = ObjectUtils.getDifferenceList(oldIds, newIds).getRemoved();

        if (ObjectUtils.isEmpty(diffRemoved)) {
            return;
        }
        calendarEventRepository.deleteAllByExternalCalendarIdIn(diffRemoved);
    }

    /**
     * get Events From External Calendar
     *
     * @param calendarIntegrate CalendarIntegration
     * @return List<ResponseCalendarEvent>
     */
    private List<ResponseCalendarEvent> getEventsFromExternalCalendar(CalendarIntegration calendarIntegrate) {
        String accessToken = getAccessTokenCalendar(calendarIntegrate);
        List<ResponseCalendarEvent> events = new ArrayList<>();
        if (ObjectUtils.isEmpty(calendarIntegrate.getCalendarRefId())) {
            return List.of();
        }
        var calendarRefIds = Arrays.stream(calendarIntegrate.getCalendarRefId().split(","))
            .filter(ele -> !ObjectUtils.isEmpty(ele))
            .toList();
        switch (calendarIntegrate.getCalendarType()) {
            case GOOGLE -> calendarRefIds.forEach(
                calendarId -> events.addAll(googleCalendarService.getEvents(calendarId, accessToken, calendarIntegrate.getId()))
            );
            case OUTLOOK -> calendarRefIds.forEach(
                calendarId ->
                    events.addAll(microsoftCalendarService.getAllEventByCalendar(calendarIntegrate.getId(), accessToken, calendarId))
            );
            case ICALENDAR -> events.addAll(iCalService.getEventsFromUrl(calendarIntegrate.getUrl(), calendarIntegrate));
        }
        return events;
    }

    /**
     * map To CalendarEvent
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
            existingEvent.setCalendarIntegration(calendarIntegrate);
        } else {
            calendarEventMapper.partialEntity(existingEvent, responseEvent);
        }
        existingEvent.setExternalEventId(responseEvent.getExternalEventId());
        return existingEvent;
    }

    /**
     * disconnect Calendar Integrate
     *
     * @param bookingSettingId UUID
     */
    private void disconnectCalendarIntegrate(UUID bookingSettingId) {
        var calendarIntegrateOpt = calendarIntegrationRepository.findByBookingSettingId(bookingSettingId);
        if (calendarIntegrateOpt.isEmpty()) {
            return;
        }
        var calendarIntegrate = calendarIntegrateOpt.get();
        calendarIntegrate.setBookingSetting(null);
        calendarIntegrationRepository.saveAndFlush(calendarIntegrate);
        calendarEventRepository.deleteAllByCalendarIntegrationId(calendarIntegrate.getId());
        calendarTokenRepository.deleteByCalendarIntegrationId(calendarIntegrate.getId());
        calendarIntegrationRepository.delete(calendarIntegrate);
    }

    /**
     * get Or Create Setting
     *
     * @param request RequestBookingSetting
     * @return Setting
     */
    private BookingSetting getOrCreateSetting(RequestBookingSetting request) {
        return bookingSettingRepository
            .findByUserId(request.getUserId())
            .map(existingSetting -> {
                bookingSettingMapper.partialEntity(existingSetting, request);
                return existingSetting;
            })
            .orElse(bookingSettingMapper.toEntity(request));
    }

    /**
     * save Data Availability Setting
     *
     * @param availabilityRequests List<RequestTechnicalAdvisorAvailability>
     * @param setting              Setting
     */
    private void saveDataAvailabilitySetting(List<RequestTechnicalAdvisorAvailability> availabilityRequests, BookingSetting setting) {
        List<Availability> currentAvailabilities = availabilityRepository.getAllByBookingSettingId(setting.getId());
        var availabilityMap = currentAvailabilities.stream().collect(Collectors.toMap(Availability::getDaysOfWeek, Function.identity()));

        List<Availability> availabilitiesToSave = new ArrayList<>();
        List<Availability> availabilitiesToDelete = new ArrayList<>(currentAvailabilities);

        for (RequestTechnicalAdvisorAvailability request : availabilityRequests) {
            if (request.getStartTime().isAfter(request.getEndTime())) {
                throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0015, "Availability Start Time"));
            }
            Availability availability = availabilityMap.get(request.getDaysOfWeek());

            if (Objects.nonNull(availability)) {
                // Update existing availability
                availabilityMapper.partialEntity(availability, request);
                availabilitiesToSave.add(availability);
                availabilitiesToDelete.remove(availability);
            } else {
                // Add new availability
                Availability newAvailability = availabilityMapper.toEntity(request);
                newAvailability.setBookingSetting(setting);
                availabilitiesToSave.add(newAvailability);
            }
        }
        // Save updated and new availabilities
        availabilityRepository.saveAll(availabilitiesToSave);
        // Delete availabilities not present in the request
        availabilityRepository.deleteAll(availabilitiesToDelete);
    }

    /**
     * validate Booking Setting
     *
     * @param request RequestBookingSetting
     */
    private void validateBookingSetting(RequestBookingSetting request, Instant originEarliestDateIns) {
        if (Objects.isNull(request.getEarliestDate())) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0016, "Earliest Appointment Date"));
        }
        // check exist technical advisor
        ZoneId zoneId = ZoneId.of(request.getTimezone());
        LocalDate today = LocalDate.ofInstant(Instant.now(), zoneId);
        LocalDate earliestDate = LocalDate.ofInstant(request.getEarliestDate(), zoneId);
        LocalDate originEarliestDate = LocalDate.ofInstant(originEarliestDateIns, zoneId);
        if (!originEarliestDate.equals(earliestDate) && today.isAfter(earliestDate)) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0033, "Earliest Appointment Date"));
        }
        if (Objects.nonNull(request.getMeetingPlatform()) && ObjectUtils.isEmpty(request.getLinkMeetingPlatform())) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0027, "Link Meeting Platform"));
        }
    }
}
