package io.yanmastra.quarkus.microservices.common.utils;

import io.quarkus.arc.Unremovable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jboss.logging.Logger;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Unremovable
@Singleton
@ParamToQueryQualifier(operator = ParamToQueryRange.KEY)
public final class ParamToQueryRange extends ParamToQuery{
    public static final String KEY = "range";

    @Inject
    Logger log;

    @Override
    public String getWhereClause(String key, List<String> value, String alias) {
        if (value.size() != 3) return "";

        String sKey = getSKey(key);
        Object startDate = getRealValue(value.get(1));
        if (startDate instanceof Date date) {
            return "( cast(" + alias + key + " as date) between :"+sKey+"_start and :"+sKey+"_end )";
        }
        return "( " + alias + key + " between :"+sKey+"_start and :"+sKey+"_end )";
    }

    @Override
    public Map<String, Object> getFieldAndParams(String key, List<String> value, String alias) {
        if (value.size() != 3) return Map.of();

        log.debug("getFieldAndParams:"+key+","+value+","+alias);
        String sKey = getSKey(key);
        return Map.of(
                sKey + "_start", getRealValue(value.get(1)),
                sKey + "_end", getRealValue(value.get(2))
        );
    }
}
