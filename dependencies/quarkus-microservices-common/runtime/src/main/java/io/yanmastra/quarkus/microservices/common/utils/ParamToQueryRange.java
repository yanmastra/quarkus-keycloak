package io.yanmastra.quarkus.microservices.common.utils;

import io.quarkus.arc.Unremovable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.Map;

@Unremovable
@Singleton
@ParamToQueryQualifier(operator = "range")
public class ParamToQueryRange extends ParamToQuery{

    @Inject
    Logger log;

    @Override
    public String getWhereClause(String key, List<String> value, String alias) {
        String sKey = getSKey(key);
        return "( " + alias + key + " between :"+sKey+"_start and :"+sKey+"_end )";
    }

    @Override
    public Map<String, Object> getFieldAndParams(String key, List<String> value, String alias) {
        log.debug("getFieldAndParams:"+key+","+value+","+alias);
        String sKey = getSKey(key);
        return Map.of(
                sKey + "_start", getRealValue(value.get(1)),
                sKey + "_end", getRealValue(value.get(2))
        );
    }
}
