package io.yanmastra.quarkus.microservices.common.utils;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Covers the per-data-type branches in ParamToQueryIn/NotIn/GreaterThan/LessThan:
 * numeric/decimal and boolean compare as-is, String is cast to string (in/notIn only -
 * greaterThan/lessThan reject strings), and date-only values are cast to date.
 */
@QuarkusTest
public class ParamToQueryTypeVariantTest {

    @Inject
    @ParamToQueryQualifier(operator = "in")
    ParamToQueryIn in;
    @Inject
    @ParamToQueryQualifier(operator = "notIn")
    ParamToQueryNotIn notIn;
    @Inject
    @ParamToQueryQualifier(operator = "greaterThan")
    ParamToQueryGreaterThan greaterThan;
    @Inject
    @ParamToQueryQualifier(operator = "lessThan")
    ParamToQueryLessThan lessThan;

    private static final String ALIAS = "";

    // ---- in ----

    @Test
    public void testInNumeric() {
        List<String> value = List.of("in", "100", "200");
        assertEquals("price in (:price)", in.getWhereClause("price", value, ALIAS));

        Set<?> values = (Set<?>) in.getFieldAndParams("price", value, ALIAS).get("price");
        assertEquals(Set.of(100L, 200L), values);
    }

    @Test
    public void testInDecimal() {
        List<String> value = List.of("in", "99.99", "50.50");
        assertEquals("price in (:price)", in.getWhereClause("price", value, ALIAS));

        Set<?> values = (Set<?>) in.getFieldAndParams("price", value, ALIAS).get("price");
        assertEquals(Set.of(new BigDecimal("99.99"), new BigDecimal("50.50")), values);
    }

    @Test
    public void testInString() {
        List<String> value = List.of("in", "foo", "bar");
        assertEquals("cast(category as string) in (:category)", in.getWhereClause("category", value, ALIAS));

        Set<?> values = (Set<?>) in.getFieldAndParams("category", value, ALIAS).get("category");
        assertEquals(Set.of("foo", "bar"), values);
    }

    @Test
    public void testInDate() {
        List<String> value = List.of("in", "2026-01-01", "2026-02-01");
        assertEquals("cast(x_date as date) in (:x_date)", in.getWhereClause("x_date", value, ALIAS));

        Set<?> values = (Set<?>) in.getFieldAndParams("x_date", value, ALIAS).get("x_date");
        assertEquals(2, values.size());
        values.forEach(v -> assertInstanceOf(Date.class, v));
    }

    @Test
    public void testInBoolean() {
        List<String> value = List.of("in", "true", "false");
        assertEquals("is_active in (:is_active)", in.getWhereClause("is_active", value, ALIAS));

        Set<?> values = (Set<?>) in.getFieldAndParams("is_active", value, ALIAS).get("is_active");
        assertEquals(Set.of(true, false), values);
    }

    // ---- notIn ----

    @Test
    public void testNotInNumeric() {
        List<String> value = List.of("notIn", "1", "2");
        assertEquals("price not in (:price)", notIn.getWhereClause("price", value, ALIAS));
    }

    @Test
    public void testNotInString() {
        List<String> value = List.of("notIn", "foo", "bar");
        assertEquals("cast(category as string) not in (:category)", notIn.getWhereClause("category", value, ALIAS));
    }

    @Test
    public void testNotInDate() {
        List<String> value = List.of("notIn", "2026-01-01", "2026-02-01");
        assertEquals("cast(x_date as date) not in (:x_date)", notIn.getWhereClause("x_date", value, ALIAS));
    }

    @Test
    public void testNotInBoolean() {
        List<String> value = List.of("notIn", "true");
        assertEquals("is_active not in (:is_active)", notIn.getWhereClause("is_active", value, ALIAS));
    }

    // ---- greaterThan ----

    @Test
    public void testGreaterThanNumeric() {
        List<String> value = List.of("greaterThan", "100");
        assertEquals("price > :price", greaterThan.getWhereClause("price", value, ALIAS));
        assertEquals(100L, greaterThan.getFieldAndParams("price", value, ALIAS).get("price"));
    }

    @Test
    public void testGreaterThanDecimal() {
        List<String> value = List.of("greaterThan", "99.99");
        assertEquals("price > :price", greaterThan.getWhereClause("price", value, ALIAS));
        assertEquals(new BigDecimal("99.99"), greaterThan.getFieldAndParams("price", value, ALIAS).get("price"));
    }

    @Test
    public void testGreaterThanDate() {
        List<String> value = List.of("greaterThan", "2026-01-01");
        assertEquals("cast(x_date as date) > :x_date", greaterThan.getWhereClause("x_date", value, ALIAS));
        assertInstanceOf(Date.class, greaterThan.getFieldAndParams("x_date", value, ALIAS).get("x_date"));
    }

    @Test
    public void testGreaterThanBoolean() {
        List<String> value = List.of("greaterThan", "true");
        assertEquals("is_active > :is_active", greaterThan.getWhereClause("is_active", value, ALIAS));
    }

    @Test
    public void testGreaterThanStringThrows() {
        List<String> value = List.of("greaterThan", "foo");
        assertThrows(IllegalArgumentException.class, () -> greaterThan.getWhereClause("category", value, ALIAS));
    }

    // ---- lessThan ----

    @Test
    public void testLessThanNumeric() {
        List<String> value = List.of("lessThan", "100");
        assertEquals("price < :price", lessThan.getWhereClause("price", value, ALIAS));
        assertEquals(100L, lessThan.getFieldAndParams("price", value, ALIAS).get("price"));
    }

    @Test
    public void testLessThanDecimal() {
        List<String> value = List.of("lessThan", "99.99");
        assertEquals("price < :price", lessThan.getWhereClause("price", value, ALIAS));
        assertEquals(new BigDecimal("99.99"), lessThan.getFieldAndParams("price", value, ALIAS).get("price"));
    }

    @Test
    public void testLessThanDate() {
        List<String> value = List.of("lessThan", "2026-01-01");
        assertEquals("cast(x_date as date) < :x_date", lessThan.getWhereClause("x_date", value, ALIAS));
        assertInstanceOf(Date.class, lessThan.getFieldAndParams("x_date", value, ALIAS).get("x_date"));
    }

    @Test
    public void testLessThanBoolean() {
        List<String> value = List.of("lessThan", "false");
        assertEquals("is_active < :is_active", lessThan.getWhereClause("is_active", value, ALIAS));
    }

    @Test
    public void testLessThanStringThrows() {
        List<String> value = List.of("lessThan", "foo");
        assertThrows(IllegalArgumentException.class, () -> lessThan.getWhereClause("category", value, ALIAS));
    }
}
