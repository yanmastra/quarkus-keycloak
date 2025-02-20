package io.onebyone.quarkus.microservices.common.utils;

import io.quarkus.arc.Unremovable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jboss.logging.Logger;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Unremovable
@Singleton
@ParamToQueryQualifier(operator = "equals")
public class ParamToQueryEquals extends ParamToQuery {

    @Inject
    Logger log;

    public ParamToQueryEquals() {
    }

    @Override
    public String getWhereClause(String key, List<String> value, String alias) {
        Object oValue = getRealValue(value.getFirst());
        String sKey = key;
        if (oValue instanceof String) {
            sKey = "cast(" + key + " as string)";
        }
        return alias + sKey + "=:" + getSKey(key);
    }

    @Override
    public Map<String, Object> getFieldAndParams(String key, List<String> value, String alias) {
        String sValue = value.getFirst();
        Object oValue = getRealValue(sValue);
        return Map.of(getSKey(key), oValue);
    }

    private Object getRealValue(String sValue) {
        if ("true".equalsIgnoreCase(sValue)) return true;
        if ("false".equalsIgnoreCase(sValue)) return false;

        if (sValue.matches("^[0-9]*$")) {
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
        return sValue;
    }
}
