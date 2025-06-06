package com.epam.aidial.ql.deserializers.sql;

import com.epam.aidial.expressions.exceptions.ParseException;
import com.epam.aidial.ql.common.model.enums.BinaryComparisonOperator;
import com.epam.aidial.ql.common.model.enums.JoinStrictness;
import com.epam.aidial.ql.common.model.enums.JoinType;
import com.epam.aidial.ql.common.model.enums.SortDirection;
import com.epam.aidial.ql.common.model.enums.UnaryComparisonOperator;
import com.epam.aidial.ql.dto.CompletableDto;
import com.epam.aidial.ql.dto.JoinDto;
import com.epam.aidial.ql.dto.LimitByDto;
import com.epam.aidial.ql.dto.QueryDto;
import com.epam.aidial.ql.dto.TableDto;
import com.epam.aidial.ql.dto.SortDto;
import com.epam.aidial.ql.dto.StringExpressionDto;
import com.epam.aidial.ql.dto.TupleDto;
import com.epam.aidial.ql.dto.UnionAllDto;
import com.epam.aidial.ql.dto.filters.AndDto;
import com.epam.aidial.ql.dto.filters.BinaryComparisonFilterDto;
import com.epam.aidial.ql.dto.filters.NotDto;
import com.epam.aidial.ql.dto.filters.OrDto;
import com.epam.aidial.ql.dto.filters.UnaryComparisonFilterDto;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class QueryParserUtilTest {

    @Test
    public void shouldParseQuery() {
        final CompletableDto actual = QueryParserUtil.parse("""
                    SELECT x, y, count(), (SELECT count() FROM table)
                    FROM (
                        SELECT x, y
                        FROM table1
                        UNION ALL
                        SELECT a3, b3
                        FROM tableA
                            ANY INNER JOIN tableB ON (a1, a2) == (b1, b2)
                    )
                    WHERE (NOT (x < 1 AND x != 0) OR y IN (1, 2, 3) OR (x, y) NOT IN (SELECT DISTINCT a, b FROM reportX))
                        AND y IS NOT NULL
                    GROUP BY x, y WITH TOTALS
                    HAVING count() >= x
                    ORDER BY count() DESC, x, y
                    LIMIT 10 BY x
                    LIMIT 10, 10
                """);

        final QueryDto expected = new QueryDto();

        final QueryDto queryInExpressions = new QueryDto();
        queryInExpressions.setExpressions(List.of(new StringExpressionDto("count()")));
        queryInExpressions.setFrom(new TableDto("table"));

        expected.setExpressions(Arrays.asList(
                new StringExpressionDto("x"),
                new StringExpressionDto("y"),
                new StringExpressionDto("count()"),
                queryInExpressions
        ));

        final QueryDto union1 = new QueryDto();
        union1.setExpressions(Arrays.asList(new StringExpressionDto("x"), new StringExpressionDto("y")));
        union1.setFrom(new TableDto("table1"));

        final QueryDto union2 = new QueryDto();
        union2.setExpressions(Arrays.asList(new StringExpressionDto("a3"), new StringExpressionDto("b3")));
        union2.setFrom(new TableDto("tableA"));
        union2.setJoin(new JoinDto(
                JoinStrictness.ANY,
                JoinType.INNER,
                new TableDto("tableB"),
                new TupleDto(Arrays.asList(new StringExpressionDto("a1"), new StringExpressionDto("a2"))),
                new TupleDto(Arrays.asList(new StringExpressionDto("b1"), new StringExpressionDto("b2")))
        ));

        final UnionAllDto unionAll = new UnionAllDto(Arrays.asList(union1, union2));
        expected.setFrom(unionAll);

        final QueryDto subquery = new QueryDto();
        subquery.setDistinct(true);
        subquery.setExpressions(Arrays.asList(new StringExpressionDto("a"), new StringExpressionDto("b")));
        subquery.setFrom(new TableDto("reportX"));
        expected.setWhere(new AndDto(Arrays.asList(
                new OrDto(Arrays.asList(
                        new NotDto(
                                new AndDto(Arrays.asList(
                                        new BinaryComparisonFilterDto(new StringExpressionDto("x"), BinaryComparisonOperator.LESS, new StringExpressionDto("1")),
                                        new BinaryComparisonFilterDto(new StringExpressionDto("x"), BinaryComparisonOperator.NOT_EQUALS, new StringExpressionDto("0"))
                                ))
                        ),
                        new BinaryComparisonFilterDto(
                                new StringExpressionDto("y"),
                                BinaryComparisonOperator.IN,
                                new TupleDto(Arrays.asList(
                                        new StringExpressionDto("1"), new StringExpressionDto("2"), new StringExpressionDto("3")
                                ))
                        ),
                        new BinaryComparisonFilterDto(
                                new TupleDto(Arrays.asList(new StringExpressionDto("x"), new StringExpressionDto("y"))),
                                BinaryComparisonOperator.NOT_IN,
                                subquery
                        )
                )),
                new UnaryComparisonFilterDto(new StringExpressionDto("y"), UnaryComparisonOperator.IS_NOT_NULL)
        )));

        expected.setGroupBy(Arrays.asList(new StringExpressionDto("x"), new StringExpressionDto("y")));
        expected.setWithTotals(true);
        expected.setHaving(new BinaryComparisonFilterDto(new StringExpressionDto("count()"), BinaryComparisonOperator.GREATER_OR_EQUALS, new StringExpressionDto("x")));
        expected.setOrderBy(Arrays.asList(
                new SortDto(new StringExpressionDto("count()"), SortDirection.DESC),
                new SortDto(new StringExpressionDto("x"), SortDirection.ASC),
                new SortDto(new StringExpressionDto("y"), SortDirection.ASC)
        ));
        expected.setLimitBy(new LimitByDto(List.of(new StringExpressionDto("x")), 10));
        expected.setOffset(10L);
        expected.setLimit(10L);

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseWithSimpleJoin() {
        final CompletableDto actual = QueryParserUtil.parse("SELECT 1 FROM A ANY LEFT JOIN B ON a == b");

        final QueryDto expected = new QueryDto();
        expected.setExpressions(List.of(new StringExpressionDto("1")));
        expected.setFrom(new TableDto("A"));
        expected.setJoin(new JoinDto(
                JoinStrictness.ANY,
                JoinType.LEFT,
                new TableDto("B"),
                List.of(new StringExpressionDto("a")),
                List.of(new StringExpressionDto("b"))
        ));

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseFunctions() {
        final CompletableDto actual = QueryParserUtil.parse("SELECT any(x) FROM table");

        final QueryDto expected = new QueryDto();
        expected.setExpressions(List.of(new StringExpressionDto("any(x)")));
        expected.setFrom(new TableDto("table"));

        assertEquals(expected, actual);
    }

    public void testException(final String query) {
        try {
            final CompletableDto actual = QueryParserUtil.parse(query);
            Assert.fail(String.format("Should throw exception on '%s', but parsed as '%s'", query, actual));
        } catch (ParseException e) {
        }
    }

    @Test
    public void shouldFail() {
        testException("SELECTx FROM table");
        testException("SELECT FROM table");
        testException("SELECT x FROM table ANYINNER JOIN table x = x");
        testException("SELECT x FROM table GROUPBY x");
        testException("SELECT x FROM table ORDERBY x");
    }
}
