package io.yanmastra.quarkusBase.utils;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.core.GenericType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class ValidationUtils {
    private ValidationUtils(){}
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9_! #$%&'*+/=?`{|}~^. -]+@[a-zA-Z0-9. -]+$");

    public static boolean isEmail(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }

    public static void fetchValidationError(ClientErrorException clientError, Map<String, String> errors) {
        Map<String, Object> responseEntity = clientError.getResponse().readEntity(new GenericType<>(Map.class));
        if (responseEntity.containsKey("errors") && responseEntity.get("errors") instanceof JsonObject jsonObject) {
            for (Map.Entry<String, JsonValue> entry : jsonObject.entrySet()) {
                fetchErrorValue(errors, entry.getKey(), entry.getValue());
            }
        } else if (responseEntity.containsKey("errors") && responseEntity.get("errors") instanceof JsonArray jsonArray) {
            for (JsonValue jsonValue : jsonArray) {
                if (jsonValue instanceof JsonObject jsonObject) {
                    fetchErrorValue(errors, jsonObject.getString("name"), jsonObject.getJsonArray("message"));
                }
            }
        }
    }

    private static void fetchErrorValue(Map<String, String> errors, String key, JsonValue value) {
        switch (value.getValueType()) {
            case OBJECT -> {
                List<String> sValue = new ArrayList<>();
                JsonObject jsonObject = value.asJsonObject();
                for (String cKey : jsonObject.keySet()) {
                    sValue.add("[" + cKey + "] " + jsonObject.getString(cKey));
                }
                errors.put(key, String.join(",", sValue));
            }
            case ARRAY -> {
                JsonArray jsonArray = value.asJsonArray();
                errors.put(key, String.join(",", Stream.of(jsonArray).map(JsonValue::toString).toList()));
            }
            case STRING, NUMBER, TRUE, FALSE -> errors.put(key, value.toString());
        }
    }
}
