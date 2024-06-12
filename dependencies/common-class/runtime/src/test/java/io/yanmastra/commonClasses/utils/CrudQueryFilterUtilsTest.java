package io.yanmastra.commonClasses.utils;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@QuarkusTest
public class CrudQueryFilterUtilsTest {

    private static final Logger log = Logger.getLogger(CrudQueryFilterUtilsTest.class);

    @Test
    public void testCreateQuery() {
        log.info("start");

        MultivaluedMap<String, String> params = new MultivaluedHashMap<>();
        params.add("keyword", "something");
        params.addAll("column1", "range", "1", "10");
        params.addAll("column2", "in", "1", "2", "3", "4", "5");
        params.addAll("column3", "notIn", "1", "2");
        params.addAll("column4", "greaterThan", "1000");
        params.addAll("column5", "lessThan", "2000");

        Map<String, Object> sqlParams = new HashMap<>();
        String whereClause = CrudQueryFilterUtils.createFilterQuery(params, sqlParams, Set.of("column1", "column2", "column3"));

        log.info("result:" + whereClause);
        log.info("values: "+sqlParams);

        params = new MultivaluedHashMap<>();
        params.add("keyword", "something");

        sqlParams = new HashMap<>();
        whereClause = CrudQueryFilterUtils.createFilterQuery(params, sqlParams, Set.of("column1", "column2", "column3"));

        log.info("\nresult:" + whereClause);
        log.info("values: "+sqlParams);

        params = new MultivaluedHashMap<>();
        params.addAll("column2", "in", "1", "2", "3", "4", "5");

        sqlParams = new HashMap<>();
        whereClause = CrudQueryFilterUtils.createFilterQuery(params, sqlParams, Set.of("column1", "column2", "column3"));

        log.info("\nresult:" + whereClause);
        log.info("values: "+sqlParams);

        params = new MultivaluedHashMap<>();
        params.addAll("column2", "range", "1000", "2000");

        sqlParams = new HashMap<>();
        whereClause = CrudQueryFilterUtils.createFilterQuery(params, sqlParams, Set.of("column1", "column2", "column3"));

        log.info("\nresult:" + whereClause);
        log.info("values: "+sqlParams);

        params = new MultivaluedHashMap<>();
        sqlParams = new HashMap<>();
        whereClause = CrudQueryFilterUtils.createFilterQuery(params, sqlParams, Set.of("column1", "column2", "column3"));

        log.info("\nresult:" + whereClause);
        log.info("values: "+sqlParams);
    }
}
