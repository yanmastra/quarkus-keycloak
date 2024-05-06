package io.yanmastra.commonClass.utils;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ParamToQueryIn extends ParamToQuery{
    ParamToQueryIn(String key, List<String> value) {
        super(key, value);
        this.key = key;
        this.sKey = key.replace(".", "_");
        for (int i = 1; i < value.size(); i++) {
            this.value.add(value.get(i));
        }
    }

    private final String key;
    private final String sKey;
    private final Set<String> value = new HashSet<>();

    @Override
    public String getWhereClause() {
        return key + " in (:"+sKey+")";
    }

    @Override
    public void attachValue(Map<String, Object> hibernateQueryParams) {
        hibernateQueryParams.put(sKey, value);
    }
}
