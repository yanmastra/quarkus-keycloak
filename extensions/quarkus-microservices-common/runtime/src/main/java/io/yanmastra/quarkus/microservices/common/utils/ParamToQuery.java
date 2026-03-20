package io.yanmastra.quarkus.microservices.common.utils;

import io.yanmastra.quarkusBase.utils.DateTimeUtils;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.List;
import java.util.Map;

public abstract class ParamToQuery {

    @Inject
    Logger log;

    private static final String REGEX_DECIMAL = "^-?\\d+(\\.\\d+)?$";
    private static final String REGEX_INTEGER = "^-?\\d+$";

    @Deprecated
    public String getWhereClause(){
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Deprecated
    public void attachValue(Map<String, Object> hibernateQueryParams) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public abstract Map<String, Object> getFieldAndParams(String key, List<String> value, String alias);
    public abstract String getWhereClause(String key, List<String> value, String alias);
    protected String getSKey(String key) {
        if (StringUtils.isBlank(key)) throw new BadRequestException("key is blank in " + this.getClass().getSimpleName());

        return key.replace(".", "_")
                .replace("(", "")
                .replace(")", "");
    }


    protected Object getRealValue(String sValue) {
        if ("true".equalsIgnoreCase(sValue)) return true;
        if ("false".equalsIgnoreCase(sValue)) return false;
        if (DateTimeUtils.isDate(sValue)) {
            Date result = DateTimeUtils.fromDateOnly(sValue);
            if (result == null) {
                result = DateTimeUtils.fromUtc(sValue);
            }
            return result;
        }

        if (sValue.length() <= 20 && sValue.matches(REGEX_INTEGER)) {
            try {
                return Long.parseLong(sValue);
            } catch (Throwable e) {
                log.warn("Parse Long failed " + e.getMessage());
            }

            try {
                return new BigDecimal(sValue);
            } catch (Throwable e) {
                log.warn("Parse BigDecimal failed " + e.getMessage());
            }
        }

        if (sValue.matches(REGEX_DECIMAL)) {
            String[] splitDecimal = sValue.split("\\.");
            if (splitDecimal.length == 2 && splitDecimal[0].length() <= 20) {
                try {
                    return new BigDecimal(sValue);
                } catch (Throwable e) {
                    log.warn("Parse BigDecimal failed " + e.getMessage());
                }
            }
        }

        if (DateTimeUtils.looksLikeDateTime(sValue)) {
            TemporalAccessor ta = DateTimeUtils.parseDateTime(sValue);
            if (ta instanceof OffsetDateTime offsetDateTime) return offsetDateTime;
            else if (ta instanceof ZonedDateTime zonedDateTime) return zonedDateTime;
        }

        return sValue;
    }
}
