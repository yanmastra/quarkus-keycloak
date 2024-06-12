package io.yanmastra.commonClasses.utils;

import java.util.List;
import java.util.Map;

public class ParamToQueryEquals extends ParamToQuery {
    private final String sKey;
    private final String value;

    public ParamToQueryEquals(String key, List<String> value, String alias) {
        super(key, value, alias);
        this.sKey = key.replace(".", "_");
        this.value = value.get(0);
    }

    @Override
    public String getWhereClause() {
        return "cast(" + alias + key + " as string)=:" + sKey;
    }

    @Override
    public void attachValue(Map<String, Object> hibernateQueryParams) {
        hibernateQueryParams.put(sKey, value);
    }
}
