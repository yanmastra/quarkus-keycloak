package io.yanmastra.quarkusBase.quarkusBase.utils;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Date;

public class DateDeserializer extends JsonDeserializer<Date> {
    @Override
    public Date deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        String json = jsonParser.getText();
        if (!StringUtils.isBlank(json)) {
            Date date = null;
            if (DateTimeUtils.isDate(json)) {
                date = DateTimeUtils.fromDateOnly(json);
            }

            if (date == null) {
                date = DateTimeUtils.fromUtc(json);
            }
            return date;
        }
        return null;
    }
}
