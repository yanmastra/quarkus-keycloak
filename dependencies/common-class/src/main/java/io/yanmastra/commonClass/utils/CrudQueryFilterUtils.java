package io.yanmastra.commonClass.utils;

import jakarta.enterprise.context.ApplicationScoped;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@ApplicationScoped
public class CrudQueryFilterUtils {

    public String createFilterQuery(String keyword, Map<String, String> otherQueries, Map<String, Object> queryParams, Set<String> searchableColumn) {
        String where = "where deletedAt is null";
        StringBuilder sbQuery = new StringBuilder(where);
        if (StringUtils.isNotBlank(keyword)) {
            queryParams.put("keyword", "%"+keyword+"%");
            Set<String> searchKey = new HashSet<>();
            sbQuery.append(" and (");
            for (String column: searchableColumn) {
                searchKey.add("cast(" + column + " as string) like :keyword");
            }
            sbQuery.append(String.join(" or ", searchKey)).append(")");
        }

        if (!otherQueries.isEmpty()) {
            sbQuery.append(" and ");
        }

        if (!otherQueries.isEmpty()) {
            Iterator<String> keyQueries = otherQueries.keySet().iterator();
            while (keyQueries.hasNext()) {
                String key = keyQueries.next();
                String sKey = key.replace(".", "_");
                queryParams.put(sKey, otherQueries.get(key));

                sbQuery.append(key).append("=:").append(sKey);
                if (keyQueries.hasNext()) sbQuery.append(" and ");
            }
        }
        return sbQuery.toString();
    }
}
