package io.onebyone.quarkus.microservices.common.utils;

import io.onebyone.quarkusBase.utils.DateTimeUtils;
import io.quarkus.arc.Unremovable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jboss.logging.Logger;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Unremovable
@Singleton
@ParamToQueryQualifier(operator = "equals")
public class ParamToQueryEquals extends ParamToQuery {

    public ParamToQueryEquals() {
    }

    @Override
    public String getWhereClause(String key, List<String> value, String alias) {
        Object oValue = getRealValue(value.getFirst());
        String sKey = key;
        if (oValue instanceof String) {
            sKey = "cast(" + key + " as string)";
        } else if (oValue instanceof Date) {
            sKey = "cast(" + key + " as date)";
        }
        return alias + sKey + "=:" + getSKey(key);
    }

    @Override
    public Map<String, Object> getFieldAndParams(String key, List<String> value, String alias) {
        String sValue = value.getFirst();
        Object oValue = getRealValue(sValue);
        return Map.of(getSKey(key), oValue);
    }

}
