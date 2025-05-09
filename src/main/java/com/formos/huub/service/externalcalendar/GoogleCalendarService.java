package com.formos.huub.service.externalcalendar;

import com.formos.huub.domain.entity.CalendarIntegration;
import com.formos.huub.domain.entity.CalendarToken;
import com.formos.huub.domain.enums.CalendarStatusEnum;
import com.formos.huub.domain.response.calendarintegrate.ResponseCalendarEvent;
import com.formos.huub.domain.response.calendarintegrate.ResponseCalendarExternal;
import com.formos.huub.framework.base.BaseService;
import com.formos.huub.framework.properties.GoogleProperties;
import com.formos.huub.repository.CalendarIntegrationRepository;
import com.formos.huub.repository.CalendarTokenRepository;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.EventDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class GoogleCalendarService extends BaseService {

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private static final String APPLICATION_NAME = "Google Calendar API With HUUB";

    private final GoogleProperties googleProperties;

    private final CalendarTokenRepository calendarTokenRepository;

    private final CalendarIntegrationRepository calendarIntegrationRepository;

    /**
     * get Microsoft Access Token
     *
     * @param calendarIntegrateId UUID
     * @return String
     */
    public String getGoogleAccessToken(UUID calendarIntegrateId) {
        var calendarIntegrate = calendarIntegrationRepository.findById(calendarIntegrateId);
        if (calendarIntegrate.isEmpty()) {
            return null;
        }
        var socialTokenOpt = calendarTokenRepository.findByCalendarIntegrationId(calendarIntegrateId);
        if (socialTokenOpt.isEmpty() || Objects.isNull(socialTokenOpt.get().getAccessToken()) || Objects.isNull(socialTokenOpt.get().getRefreshToken())) {
            return null;
        }
        var socialToken = socialTokenOpt.get();
        if (Instant.now().isAfter(socialToken.getTokenExpireTime())) {
            return refreshAccessToken(socialToken.getRefreshToken(), calendarIntegrate.get());
        }
        return socialToken.getAccessToken();
    }

    /**
     * update Access Token
     *
     * @param accessToken         String
     * @param refreshToken        String
     * @param expireDate          Long
     * @param calendarIntegration CalendarIntegration
     */
    public void updateAccessToken(String accessToken, String refreshToken, Long expireDate, CalendarIntegration calendarIntegration, Boolean isNewToken) {
        var socialTokenOpt = calendarTokenRepository.findByCalendarIntegrationId(calendarIntegration.getId());
        CalendarToken calendarToken;
        calendarToken = socialTokenOpt.orElseGet(CalendarToken::new);
        calendarToken.setAccessToken(accessToken);
        if (Boolean.TRUE.equals(isNewToken) && Objects.nonNull(refreshToken)) {
            calendarToken.setRefreshToken(refreshToken);
        }
        calendarToken.setTokenExpireTime(Instant.now().plus(expireDate * 1000L, ChronoUnit.MILLIS));
        calendarToken.setCalendarIntegration(calendarIntegration);
        calendarTokenRepository.save(calendarToken);
    }

    /**
     * refresh Access Token
     *
     * @param refreshToken        String
     * @param calendarIntegration CalendarIntegration
     * @return String
     */
    public String refreshAccessToken(String refreshToken, CalendarIntegration calendarIntegration) {
        try {
            TokenResponse tokenResponse = new GoogleRefreshTokenRequest(
                new NetHttpTransport(), new GsonFactory(),
                refreshToken, googleProperties.getClientId(),
                googleProperties.getClientSecret())
                .setGrantType("refresh_token")
                .execute();
            if (Objects.isNull(tokenResponse) || Objects.isNull(tokenResponse.getAccessToken())) {
                return null;
            }
            updateAccessToken(tokenResponse.getAccessToken(), tokenResponse.getRefreshToken(), tokenResponse.getExpiresInSeconds(), calendarIntegration, true);
            return tokenResponse.getAccessToken();
        } catch (Exception e) {
            log.warn("Refresh Token expire. Please login again.");
            handleUseServiceFailure(calendarIntegration.getId(), "Refresh Token expire. Please login again.");
            return null;
        }
    }

    /**
     * get Calendar List
     *
     * @param accessToken String
     * @return List<CalendarListEntry>
     */
    public List<ResponseCalendarExternal> getCalendarList(String accessToken, UUID calendarIntegrationId) {
        try {
            Calendar service = getCalendarService(accessToken);
            CalendarList calendarList = service.calendarList().list().execute();
            return calendarList.getItems().stream().map(ele -> {
                var item = new ResponseCalendarExternal();
                BeanUtils.copyProperties(ele, item);
                return item;
            }).toList();
        } catch (Exception e) {
            handleUseServiceFailure(calendarIntegrationId, e.getLocalizedMessage());
        }
        return List.of();
    }


    /**
     * get Events in google by calendar id
     *
     * @param calendarId  String
     * @param accessToken String
     * @return Events
     */
    public List<ResponseCalendarEvent> getEvents(String calendarId, String accessToken, UUID calendarIntegrationId) {
        try {
            Calendar service = getCalendarService(accessToken);
            var now = Instant.now();
            Date timeMin = Date.from(now);
            Date timeMax = Date.from(now.plus(365, ChronoUnit.DAYS));
            return service.events().list(calendarId)
                .setMaxResults(1000)
                .setTimeMin(new DateTime(timeMin))
                .setTimeMax(new DateTime(timeMax))
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute().getItems().stream()
                .map(ele -> {
                    var item = new ResponseCalendarEvent();
                    BeanUtils.copyProperties(ele, item);
                    item.setExternalEventId(ele.getId());
                    item.setStartTime(convertEventDateTimeToInstant(ele.getStart()));
                    item.setEndTime(convertEventDateTimeToInstant(ele.getEnd()));
                    if (Objects.nonNull(ele.getStart())) {
                        item.setTimezone(ele.getStart().getTimeZone());
                    }
                    item.setSubject(ele.getSummary());
                    item.setExternalCalendarId(calendarId);
                    return item;
                }).toList();
        } catch (Exception e) {
            handleUseServiceFailure(calendarIntegrationId, e.getLocalizedMessage());
        }
        return List.of();
    }


    /**
     * convert EventDateTime To Instant
     *
     * @param eventDateTime EventDateTime
     * @return Instant
     */
    private Instant convertEventDateTimeToInstant(EventDateTime eventDateTime) {
        DateTime dateTime = eventDateTime.getDateTime();
        if (Objects.isNull(dateTime)) {
            dateTime = eventDateTime.getDate();
        }
        // Convert DateTime to Instant
        return Instant.ofEpochMilli(dateTime.getValue());
    }

    /**
     * get Service google
     *
     * @param accessToken String
     * @return Calendar
     */
    public Calendar getCalendarService(String accessToken) throws GeneralSecurityException, IOException {
        return new Calendar.Builder(
            GoogleNetHttpTransport.newTrustedTransport(),
            JSON_FACTORY,
            null)
            .setApplicationName(APPLICATION_NAME)
            .setHttpRequestInitializer(request -> request.getHeaders().setAuthorization("Bearer " + accessToken))
            .build();
    }

    /**
     * handle Use Service Failure
     *
     * @param calendarIntegrationId UUID
     * @param message               String
     */
    private void handleUseServiceFailure(UUID calendarIntegrationId, String message) {
        var calendarIntegrationOpt = calendarIntegrationRepository.findById(calendarIntegrationId);
        if (calendarIntegrationOpt.isEmpty()) {
            return;
        }
        var calendarIntegration = calendarIntegrationOpt.get();
        calendarIntegration.setCalendarStatus(CalendarStatusEnum.ERROR);
        calendarIntegration.setProblem(message);
        calendarIntegrationRepository.save(calendarIntegration);
    }
}
