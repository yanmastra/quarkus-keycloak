package io.yanmastra.quarkusBase.utils;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.time.LocalDate;


public class LocalDateDeserializer extends JsonDeserializer<LocalDate> {
    @Override
    public LocalDate deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        String json = jsonParser.getText();
        if (!StringUtils.isBlank(json)) {
            LocalDate date = null;
            if (DateTimeUtils.isDate(json)) {
                date = DateTimeUtils.toLocalDate(json);
            }

            if (date == null) {
                date = DateTimeUtils.fromUtcToLocalDate((json));
            }
            return date;
        }
        return null;
    }
}
