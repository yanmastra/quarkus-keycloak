package io.yanmastra.quarkusBase.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.jackson.ObjectMapperCustomizer;
import io.yanmastra.quarkusBase.utils.JsonUtils;

public final class RegisterCustomizeModule implements ObjectMapperCustomizer {
    @Override
    public void customize(ObjectMapper objectMapper) {
        JsonUtils.setObjectMapper(objectMapper);
    }
}
