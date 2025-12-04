package io.yanmastra.authentication.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.yanmastra.quarkusBase.utils.JsonUtils;
import io.quarkus.jackson.ObjectMapperCustomizer;
import jakarta.inject.Singleton;

@Singleton
public final class RegisterCustomizeModule implements ObjectMapperCustomizer {

    @Override
    public void customize(ObjectMapper objectMapper) {
        JsonUtils.setObjectMapper(objectMapper);
    }
}
