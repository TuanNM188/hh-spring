package com.formos.huub.domain.response.webhook.eventbrite;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class ResponseEventbritePayload {

    private EventConfig config;

    @JsonProperty("api_url")
    private String apiUrl;

    @Getter
    @Setter
    public static class EventConfig {

        private String action;

        @JsonProperty("endpoint_url")
        private String endpointUrl;

        @JsonProperty("user_id")
        private String userId;

        @JsonProperty("webhook_id")
        private String webhookId;
    }

    public static String toJsonFromPayload(Map<String, Object> payload) {
        ObjectMapper objectMapper = new ObjectMapper();
        var responsePayload = objectMapper.convertValue(payload, ResponseEventbritePayload.class);
        return responsePayload.toJson();
    }

    public String toJson() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static ResponseEventbritePayload fromJson(String json) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(json, ResponseEventbritePayload.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
