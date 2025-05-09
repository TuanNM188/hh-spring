package com.formos.huub.config;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GsonConfiguration {

    @Bean
    public Gson gson () {
        return new Gson();
    }


    @Converter(autoApply = true)
    public static class JsonElementConverter implements AttributeConverter<JsonElement, String> {

        private final Gson gson = new Gson();

        @Override
        public String convertToDatabaseColumn(JsonElement attribute) {
            return gson.toJson(attribute);
        }

        @Override
        public JsonElement convertToEntityAttribute(String dbData) {
            return gson.fromJson(dbData, JsonElement.class);
        }
    }
}
