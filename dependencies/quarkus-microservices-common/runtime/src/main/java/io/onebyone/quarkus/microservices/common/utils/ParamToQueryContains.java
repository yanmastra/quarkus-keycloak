package io.onebyone.quarkus.microservices.common.utils;

import io.quarkus.arc.Unremovable;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import org.jboss.logging.Logger;

import java.util.*;

@Unremovable
@Singleton
@ParamToQueryQualifier(operator = "contains")
@Priority(0)
public class ParamToQueryContains extends ParamToQuery {

    @Inject
    EntityManager em;
    @Inject
    Logger log;

    public static final String OR = " or ";
    public static final String AND = " and ";

    @Override
    public Map<String, Object> getFieldAndParams(String key, List<String> value, String alias) {
        List<String> ids = getExactValues(value);
        String entityName = getEntityName(value);
        String primaryField = getPrimaryField();
        String query = "select E from " + entityName + " E where E.deletedAt is null and E." + primaryField + " in :ids";
        List<Object> entities = null;

        try {
            entities = em.createQuery(query)
                    .setParameter("ids", ids)
                    .getResultList();
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not get entities from entityName: " + entityName + ", primaryField: " + primaryField + ", exactValues: "+ids, e);
        }

        log.debug("query: "+ query);
        log.debug("params: "+ ids);
        log.debug("found " + entities.size() + " entities");

        Map<String, Object> result = new HashMap<>();
        for (int i = 2; i < value.size(); i++) {
            int index = (i-2);
            String sKey = alias + key + index;
            result.put(sKey, entities.size() <= index ? null : entities.get(index));
        }
        return result;
    }

    protected List<String> getExactValues(List<String> value) {
        return value.subList(2, value.size());
    }

    protected String getPrimaryField() {
        return "id";
    }

    protected String getLogicOperator() {
        return OR;
    }

    protected String getEntityName(List<String> values){
        return values.get(1);
    }

    @Override
    public String getWhereClause(String key, List<String> value, String alias) {
        Set<String> clauses = new HashSet<>();
        for (int i = 0; i < getExactValues(value).size(); i++) {
            String sKey = ":" + key + i;
            clauses.add(sKey + " in elements("+ alias + key + ")");
        }

        String op = getLogicOperator();
        if (!OR.equals(op) && !AND.equals(op)) {
            throw new IllegalArgumentException("LogicOperator not supported");
        }
        return String.join(getLogicOperator(), clauses);
    }
}
