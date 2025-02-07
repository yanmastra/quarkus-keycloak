package io.onebyone.authentication.provider;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.onebyone.authentication.utils.DateTimeUtils;
import io.onebyone.authentication.utils.JsonUtils;
import io.quarkus.jackson.ObjectMapperCustomizer;
import jakarta.inject.Singleton;

import java.text.SimpleDateFormat;

@Singleton
public class RegisterCustomizeModule implements ObjectMapperCustomizer {

    @Override
    public void customize(ObjectMapper objectMapper) {
        JsonUtils.setObjectMapper(objectMapper);
    }
}
