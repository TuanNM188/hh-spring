package com.formos.huub.service.externalcalendar;

import com.formos.huub.domain.constant.MicrosoftConstant;
import com.formos.huub.domain.entity.CalendarIntegration;
import com.formos.huub.domain.entity.CalendarToken;
import com.formos.huub.domain.enums.CalendarStatusEnum;
import com.formos.huub.domain.response.calendarintegrate.*;
import com.formos.huub.framework.base.BaseService;
import com.formos.huub.framework.enums.DateTimeFormat;
import com.formos.huub.framework.properties.MicrosoftProperties;
import com.formos.huub.framework.utils.DateUtils;
import com.formos.huub.repository.CalendarIntegrationRepository;
import com.formos.huub.repository.CalendarTokenRepository;
import com.formos.huub.service.resttemplate.RestTemplateService;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MicrosoftCalendarService extends BaseService {

    private final CalendarTokenRepository calendarTokenRepository;

    private final MicrosoftProperties microsoftProperties;

    private final CalendarIntegrationRepository calendarIntegrationRepository;

    private final RestTemplateService restTemplateService;

    /**
     * get Google Access Token
     *
     * @param calendarIntegrateId UUID
     * @return String
     */
    public String getMicrosoftAccessToken(UUID calendarIntegrateId) {
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
            return getRefreshAccessToken(socialToken.getRefreshToken(), calendarIntegrate.get());
        }
        return socialToken.getAccessToken();
    }

    /**
     * initialize Headers
     *
     * @param accessToken String
     * @return HttpHeaders
     */
    public HttpHeaders initializeHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    /**
     * get Refresh Access Token
     *
     * @param refreshToken        String
     * @param calendarIntegration CalendarIntegration
     * @return String
     */
    public String getRefreshAccessToken(String refreshToken, CalendarIntegration calendarIntegration) {
        try {
            var accessToken = refreshAccessToken(refreshToken);
            if (Objects.isNull(accessToken) || Objects.isNull(accessToken.getAccess_token())) {
                return null;
            }
            updateAccessToken(accessToken.getAccess_token(), accessToken.getRefresh_token(), 3600L, calendarIntegration, true);
            return accessToken.getAccess_token();
        } catch (Exception e) {
            log.warn("Refresh Token expire. Please login again.");
            handleUseServiceFailure(calendarIntegration.getId(), "Refresh Token expire. Please login again.");
            return null;
        }
    }

    /**
     * get All Calendar User
     *
     * @param calendarIntegrationId UUID
     * @param accessToken           String
     * @return ResponseOutlookListCalendar
     */
    public List<ResponseCalendarExternal> getAllCalendarUser(UUID calendarIntegrationId, String accessToken) {
        HttpHeaders httpHeaders = initializeHeaders(accessToken);
        var url = MicrosoftConstant.BASE_URL.concat(MicrosoftConstant.URL_LIST_CALENDAR);
        var responseEntity = restTemplateService.sendGetRequest(url, httpHeaders, ResponseOutlookListCalendar.class);

        if (!responseEntity.getStatusCode().is2xxSuccessful() || Objects.isNull(responseEntity.getBody())) {
            log.warn("MICROSOFT::: Get all Calendar outlook on microsoft Fail.");
            var gson = new Gson();
            handleUseServiceFailure(calendarIntegrationId, gson.toJson(responseEntity));
            return List.of();
        }
        log.warn("MICROSOFT:::Get all Calendar outlook on microsoft success.");
        return responseEntity.getBody().getValue().stream().map(ele -> {
            var item = new ResponseCalendarExternal();
            item.setId(ele.getId());
            item.setSummary(ele.getName());
            return item;
        }).toList();
    }

    /**
     * get All Event By Calendar
     *
     * @param calendarIntegrationId UUID
     * @param accessToken           String
     * @return ResponseOutlookListEvent
     */
    public List<ResponseCalendarEvent> getAllEventByCalendar(UUID calendarIntegrationId, String accessToken, String calendarId) {
        HttpHeaders httpHeaders = initializeHeaders(accessToken);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DateTimeFormat.ISO_8601_BASIC_DATE_PATTERN.getValue())
            .withZone(ZoneOffset.UTC);
        var now = Instant.now();
        var timeMin = formatter.format(now);
        var timeMax = formatter.format(now.plus(356, ChronoUnit.DAYS));

        var filter = String.format("?$filter=start/dateTime ge '%s' and end/dateTime le '%s'", timeMin, timeMax);
        var url = MicrosoftConstant.BASE_URL.concat(MicrosoftConstant.URL_EVENTS_BY_CALENDAR.replace("{calendarId}",calendarId)).concat(filter);

        var responseEntity = restTemplateService.sendGetRequest(url, httpHeaders, ResponseOutlookListEvent.class);
        if (!responseEntity.getStatusCode().is2xxSuccessful() || Objects.isNull(responseEntity.getBody())) {
            log.warn("MICROSOFT::: Get all event outlook on microsoft Fail.");
            var gson = new Gson();
            handleUseServiceFailure(calendarIntegrationId, gson.toJson(responseEntity));
            return List.of();
        }
        log.warn("MICROSOFT:::Get all event outlook on microsoft success.");
        var value = responseEntity.getBody().getValue();
        return value.stream().map(ele ->{
            var item = new ResponseCalendarEvent();
            item.setExternalCalendarId(calendarId);
            item.setExternalEventId(ele.getId());
            item.setLocation(Objects.nonNull(ele.getLocation()) ? ele.getLocation().getDisplayName() : null);
            item.setTimezone(ele.getStart().getTimeZone());
            item.setBody(ele.getBody().getContent());
            item.setSubject(ele.getSubject());
            item.setStartTime(DateUtils.convertUtcStringToInstant(ele.getStart().getDateTime()));
            item.setEndTime(DateUtils.convertUtcStringToInstant(ele.getEnd().getDateTime()));
            return item;
        }).toList();
    }

    /**
     * refresh Access Token
     *
     * @param refreshToken String
     * @return ResponseMicrosoftToken
     */
    public ResponseMicrosoftToken refreshAccessToken(String refreshToken) throws IOException {
        var httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(MicrosoftConstant.URL_REFRESH_URL);
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        var clientId = microsoftProperties.getClientId();
        String requestStr = String.format("grant_type=refresh_token&client_id=%s&refresh_token=%s", clientId, refreshToken);
        httpPost.setEntity(new StringEntity(requestStr, ContentType.APPLICATION_FORM_URLENCODED));
        var response = httpClient.execute(httpPost);
        var entity = response.getEntity();
        if (entity != null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()))) {
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line);
                }
                var gson = new Gson();
                return gson.fromJson(content.toString(), ResponseMicrosoftToken.class);
            }
        }
        log.warn("MICROSOFT:::No response body.");
        return null;
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
