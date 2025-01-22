package io.yanmastra.quarkus.microservices.common.utils;

import io.quarkus.arc.Arc;
import io.quarkus.arc.InstanceHandle;
import jakarta.ws.rs.BadRequestException;
import org.jboss.logging.Logger;

import java.util.List;

public class ParamToQueryFactory {
    private static final Logger log = Logger.getLogger(ParamToQueryFactory.class);

    static ParamToQuery find(List<String> value) {
        ParamToQuery paramToQuery = null;
        String operator = "";

        log.debug("value: " + value);

        if (value.size() > 1) {
            operator = value.getFirst();
        } else if (value.size() == 1) {
            if ("isNull".equals(value.get(0))) {
                operator = "isNull";
            } else if ("isNotNull".equals(value.get(0))) {
                operator = "isNotNull";
            } else
                operator = "equals";
        } else
            throw new BadRequestException("Unknown operator in : " + value);

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
