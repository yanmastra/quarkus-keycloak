package io.onebyone.quarkusBase.utils;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.std.DateDeserializers;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.std.DateSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.jboss.logging.Logger;

import java.text.SimpleDateFormat;
import java.util.Date;

public class JsonUtils {
    private static final Logger logger = Logger.getLogger(JsonUtils.class);

    private JsonUtils() {
    }

    private static ObjectMapper objectMapper;

    public static ObjectMapper getObjectMapper() {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
            configure(objectMapper);
        }
        return objectMapper;
    }

    public static void setObjectMapper(ObjectMapper objectMapper) {
        if (JsonUtils.objectMapper == null) {
            JsonUtils.objectMapper = objectMapper;
            configure(JsonUtils.objectMapper);
        }
    }

    private static void configure(ObjectMapper objectMapper) {
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_UNWRAPPED_TYPE_IDENTIFIERS, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
        objectMapper.setDateFormat(new SimpleDateFormat(DateTimeUtils.ZONED_DATE_TIME_FORMAT));

        SimpleModule module = new JavaTimeModule();
        module.addDeserializer(Date.class, new DateDeserializer());
        module.addSerializer(Date.class, new DateSerializer());
        objectMapper.registerModule(module);
        objectMapper.setVisibility(objectMapper.getSerializationConfig()
                .getDefaultVisibilityChecker()
                .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE));
    }

    public static String toJson(Object object, boolean throwException) {
        try {
            return getObjectMapper().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            if (throwException) throw new RuntimeException(e);
            else {
                logger.error(e.getMessage(), e);
                return null;
            }
        }
    }

    public static String toJson(Object object) {
        return toJson(object, false);
    }

    public static <E> E fromJson(String json, TypeReference<E> typeReference, boolean throwException) {
        try {
            return getObjectMapper().readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            if (throwException) throw new RuntimeException(e);
            else {
                logger.error(e.getMessage(), e);
                return null;
            }
        }
    }

    public static <E> E fromJson(String json, Class<E> eClass, boolean throwException) {
        try {
            return getObjectMapper().readValue(json, eClass);
        } catch (JsonProcessingException e) {
            if (throwException) throw new RuntimeException(e);
            else {
                logger.error(e.getMessage(), e);
                return null;
            }
        }
    }

    public static <E> E fromJson(String json, Class<E> eClass) {
        return fromJson(json, eClass, false);
    }

    public static <E> E fromJson(String json, TypeReference<E> typeReference) {
        return fromJson(json, typeReference, false);
    }
}
