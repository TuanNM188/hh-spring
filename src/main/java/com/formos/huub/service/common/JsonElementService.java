package com.formos.huub.service.common;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JsonElementService {

    private final Gson gson;

    public String convertJsonElementToString(JsonElement jsonElement) {
        return gson.toJson(jsonElement);
    }

    public JsonElement convertStringToJsonElement(String jsonString) {
        return gson.toJsonTree(jsonString);
    }

    public JsonElement getValueByKey(JsonElement jsonElement, String key) {
        if (jsonElement.isJsonObject()) {
            return jsonElement.getAsJsonObject().get(key);
        }
        return null;
    }

    public <T> T convertJsonElementToDto(JsonElement jsonElement, Class<T> dtoClass) {
        return gson.fromJson(jsonElement, dtoClass);
    }

    public JsonElement convertDtoToJsonElement(Object dto) {
        return gson.toJsonTree(dto);
    }
}
