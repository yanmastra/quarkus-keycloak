package io.yanmastra.commonClass.utils;

import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CrudQueryFilterUtilsTest {

    @Test
    public void testCreateQuery() {
        System.out.println("start");
        CrudQueryFilterUtils utils = new CrudQueryFilterUtils();

        MultivaluedMap<String, String> params = new MultivaluedHashMap<>();
        params.add("keyword", "something");
        params.addAll("column1", "range", "1", "10");
        params.addAll("column2", "in", "1", "2", "3", "4", "5");
        params.addAll("column3", "notIn", "1", "2");
        params.addAll("column4", "greaterThan", "1000");
        params.addAll("column5", "lessThan", "2000");

        Map<String, Object> sqlParams = new HashMap<>();
        String whereClause = utils.createFilterQuery(params, sqlParams, Set.of("column1", "column2", "column3"));

        System.out.println("result:" + whereClause);
        System.out.println("values: "+sqlParams);

        params = new MultivaluedHashMap<>();
        params.add("keyword", "something");

        sqlParams = new HashMap<>();
        whereClause = utils.createFilterQuery(params, sqlParams, Set.of("column1", "column2", "column3"));

        System.out.println("\nresult:" + whereClause);
        System.out.println("values: "+sqlParams);

        params = new MultivaluedHashMap<>();
        params.addAll("column2", "in", "1", "2", "3", "4", "5");

        sqlParams = new HashMap<>();
        whereClause = utils.createFilterQuery(params, sqlParams, Set.of("column1", "column2", "column3"));

        System.out.println("\nresult:" + whereClause);
        System.out.println("values: "+sqlParams);

        params = new MultivaluedHashMap<>();
        params.addAll("column2", "range", "1000", "2000");

        sqlParams = new HashMap<>();
        whereClause = utils.createFilterQuery(params, sqlParams, Set.of("column1", "column2", "column3"));

        System.out.println("\nresult:" + whereClause);
        System.out.println("values: "+sqlParams);

        params = new MultivaluedHashMap<>();
        sqlParams = new HashMap<>();
        whereClause = utils.createFilterQuery(params, sqlParams, Set.of("column1", "column2", "column3"));

        System.out.println("\nresult:" + whereClause);
        System.out.println("values: "+sqlParams);
    }
}
