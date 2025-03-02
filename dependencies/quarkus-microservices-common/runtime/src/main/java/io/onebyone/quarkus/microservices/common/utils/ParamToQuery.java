package io.onebyone.quarkus.microservices.common.utils;

import jakarta.ws.rs.BadRequestException;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

public abstract class ParamToQuery {

    @Deprecated
    public String getWhereClause(){
        throw new UnsupportedOperationException("Not supported yet.");
    };

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
}
