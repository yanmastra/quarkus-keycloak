package io.yanmastra.commonClass.utils;

import java.util.List;
import java.util.Map;

public class ParamToQueryEquals extends ParamToQuery {
    private final String key;
    private final String sKey;
    private final String value;

    public ParamToQueryEquals(String key, List<String> value) {
        super(key, value);
        this.key = key;
        this.sKey = key.replace(".", "_");
        this.value = value.get(0);
    }

    @Override
    public String getWhereClause() {
        return key + "=:" + sKey;
    }

    @Override
    public void attachValue(Map<String, Object> hibernateQueryParams) {
        hibernateQueryParams.put(sKey, value);
    }
}
