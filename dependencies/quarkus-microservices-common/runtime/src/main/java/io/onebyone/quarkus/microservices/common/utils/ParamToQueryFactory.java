package io.onebyone.quarkus.microservices.common.utils;

import io.quarkus.arc.Arc;
import io.quarkus.arc.InstanceHandle;
import jakarta.ws.rs.BadRequestException;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;

import java.util.List;

public class ParamToQueryFactory {
    private static final Logger log = Logger.getLogger(ParamToQueryFactory.class);

    static ParamToQuery find(String key, List<String> value) {
        ParamToQuery paramToQuery = null;
        String operator = "";
        if (value.size() > 1) {
            operator = value.getFirst();
        } else if (value.size() == 1) {
            if ("isNull".equals(value.getFirst())) {
                operator = "isNull";
            } else if ("isNotNull".equals(value.getFirst())) {
                operator = "isNotNull";
            } else if (StringUtils.isNotBlank(value.getFirst())) {
                operator = "equals";
            }
        }

        if (StringUtils.isBlank(operator)) {
            throw new BadRequestException("Unable to prepare query parameter " + key);
        }

        try (InstanceHandle<ParamToQuery> instances = Arc.container().instance(ParamToQuery.class, ParamToQueryQualifier.Literal.of(operator))) {
            if (instances.isAvailable()) {
                paramToQuery = instances.get();
            }
        } catch (Exception e) {
            throw new BadRequestException("Error occurred while getting ParamToQuery", e);
        }

        if (paramToQuery == null) {
            throw new BadRequestException("Error occurred while getting ParamToQuery");
        }
        return paramToQuery;
    }
}
