package com.epam.aidial.ql;

import com.epam.aidial.expressions.Column;
import com.epam.aidial.expressions.Expression;
import com.epam.aidial.expressions.FunctionsDatasource;
import com.epam.aidial.expressions.enums.Type;
import com.epam.aidial.expressions.exceptions.ParseException;
import com.epam.aidial.expressions.impl.AggregationFunctionCallImpl;
import com.epam.aidial.expressions.impl.ColumnImpl;
import com.epam.aidial.expressions.impl.ConstantImpl;
import com.epam.aidial.expressions.impl.DummyFunctionDatasourceImpl;
import com.epam.aidial.expressions.impl.FunctionImpl;
import com.epam.aidial.expressions.impl.NumberConstantImpl;
import com.epam.aidial.ql.common.model.enums.BinaryComparisonOperator;
import com.epam.aidial.ql.common.model.enums.JoinStrictness;
import com.epam.aidial.ql.common.model.enums.JoinType;
import com.epam.aidial.ql.common.model.enums.SortDirection;
import com.epam.aidial.ql.common.model.enums.UnaryComparisonOperator;
import com.epam.aidial.ql.dto.JoinDto;
import com.epam.aidial.ql.dto.LimitByDto;
import com.epam.aidial.ql.dto.QueryDto;
import com.epam.aidial.ql.dto.SortDto;
import com.epam.aidial.ql.dto.StringExpressionDto;
import com.epam.aidial.ql.dto.TableDto;
import com.epam.aidial.ql.dto.TupleDto;
import com.epam.aidial.ql.dto.UnionAllDto;
import com.epam.aidial.ql.dto.filters.AndDto;
import com.epam.aidial.ql.dto.filters.BinaryComparisonFilterDto;
import com.epam.aidial.ql.dto.filters.NotDto;
import com.epam.aidial.ql.dto.filters.UnaryComparisonFilterDto;
import com.epam.aidial.ql.model.Query;
import com.epam.aidial.ql.model.Table;
import com.epam.aidial.ql.model.filters.impl.AndImpl;
import com.epam.aidial.ql.model.filters.impl.BinaryComparisonFilterImpl;
import com.epam.aidial.ql.model.filters.impl.NotImpl;
import com.epam.aidial.ql.model.filters.impl.UnaryComparisonFilterImpl;
import com.epam.aidial.ql.model.impl.JoinImpl;
import com.epam.aidial.ql.model.impl.LimitByImpl;
import com.epam.aidial.ql.model.impl.QueryImpl;
import com.epam.aidial.ql.model.impl.SortImpl;
import com.epam.aidial.ql.model.impl.TableImpl;
import com.epam.aidial.ql.model.impl.TupleImpl;
import com.epam.aidial.ql.model.impl.UnionAllImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class LanguageConverterTests {
    @Mock
    private Engine reportEngine;

    private LanguageConverter languageConverter;

    private static final FunctionsDatasource functionsDatasource = new DummyFunctionDatasourceImpl();

    @Before
    public void init() {
        Mockito.when(reportEngine.getFunctions()).thenReturn(functionsDatasource);
        languageConverter = new LanguageConverter(reportEngine);
    }

    @Test
    public void shouldConvertSimpleQuery() {
        final QueryDto query = new QueryDto();
        query.setExpressions(Arrays.asList(new StringExpressionDto("x"), new StringExpressionDto("1"), new StringExpressionDto("'a'")));
        query.setFrom(new TableDto("report1"));

        final Column column = new ColumnImpl(Type.INT_32, "x");
        final Table table = TableImpl.builder()
                .name("report1")
                .columns(Collections.singletonMap("x", column))
                .build();

        final Query expected = QueryImpl.builder()
                .expression(column)
                .expression(NumberConstantImpl.valueOf(1L))
                .expression(new ConstantImpl(Type.STRING, "a"))
                .from(table)
                .build();

        Assert.assertEquals(expected, languageConverter.convert(query, Collections.singletonMap("report1", table)));
    }

    @Test(expected = ParseException.class)
    public void shouldNotFoundReport() {
        final QueryDto query = new QueryDto();
        query.setExpressions(Arrays.asList(new StringExpressionDto("x")));
        query.setFrom(new TableDto("report1"));

        languageConverter.convert(query, Collections.emptyMap());
    }

    @Test
    public void shouldConvertQueryWithGrouping() {
        final QueryDto query = new QueryDto();
        query.setExpressions(Arrays.asList(new StringExpressionDto("x"), new StringExpressionDto("1"), new StringExpressionDto("count()")));
        query.setFrom(new TableDto("report1"));
        query.setGroupBy(Arrays.asList(new StringExpressionDto("x")));

        final Column column = new ColumnImpl(Type.INT_32, "x");
        final Table table = TableImpl.builder()
                .name("report1")
                .columns(Collections.singletonMap("x", column))
                .build();

        final Query expected = QueryImpl.builder()
                .expression(column)
                .expression(NumberConstantImpl.valueOf(1L))
                .expression(new AggregationFunctionCallImpl(new FunctionImpl("count", Type.INT_64), Collections.emptyList(), Collections.emptyList()))
                .from(table)
                .groupBy(List.of(column))
                .build();

        Assert.assertEquals(expected, languageConverter.convert(query, Collections.singletonMap("report1", table)));
    }

    @Test(expected = ParseException.class)
    public void shouldReturnFoundColumnNotUnderAggregationFunction() {
        final QueryDto query = new QueryDto();
        query.setExpressions(Arrays.asList(new StringExpressionDto("x"), new StringExpressionDto("count()")));
        query.setFrom(new TableDto("report1"));

        final Column column = new ColumnImpl(Type.INT_32, "x");
        final Table table = TableImpl.builder()
                .name("report1")
                .columns(Collections.singletonMap("x", column))
                .build();

        languageConverter.convert(query, Collections.singletonMap("report1", table));
    }

    @Test(expected = ParseException.class)
    public void shouldReturnColumnNotGroupBy() {
        final QueryDto query = new QueryDto();
        query.setExpressions(Arrays.asList(new StringExpressionDto("x"), new StringExpressionDto("y")));
        query.setFrom(new TableDto("report1"));
        query.setGroupBy(List.of(new StringExpressionDto("x")));

        final Table table = TableImpl.builder()
                .name("report1")
                .column("x", new ColumnImpl(Type.INT_32, "x"))
                .column("y", new ColumnImpl(Type.INT_32, "y"))
                .build();

        languageConverter.convert(query, Collections.singletonMap("report1", table));
    }

    @Test
    public void shouldConvertQueryWithGroupingAndSubqueryInExpressions() {
        final QueryDto query = new QueryDto();

        final QueryDto subquery = new QueryDto();
        subquery.setExpressions(List.of(new StringExpressionDto("sum(a)")));
        subquery.setFrom(new TableDto("report2"));

        query.setExpressions(Arrays.asList(new StringExpressionDto("x"), subquery));
        query.setFrom(new TableDto("report1"));
        query.setGroupBy(List.of(new StringExpressionDto("x")));

        final Column column = new ColumnImpl(Type.INT_32, "x");
        final Table table = TableImpl.builder()
                .name("report1")
                .columns(Collections.singletonMap("x", column))
                .build();

        final Column columnA = new ColumnImpl(Type.INT_32, "a");
        final Table table2 = TableImpl.builder()
                .name("report2")
                .columns(Collections.singletonMap("a", columnA))
                .build();

        final Query expected = QueryImpl.builder()
                .expression(column)
                .expression(QueryImpl.builder()
                        .expression(new AggregationFunctionCallImpl(new FunctionImpl("sum", Type.INT_64), Collections.emptyList(), Collections.singletonList(columnA)))
                        .from(table2)
                        .build()
                )
                .from(table)
                .groupBy(List.of(column))
                .build();

        final Map<String, Table> reports = new HashMap<>();
        reports.put("report1", table);
        reports.put("report2", table2);

        Assert.assertEquals(expected, languageConverter.convert(query, reports));
    }

    @Test
    public void shouldConvertComplexQuery() {
        final QueryDto query = new QueryDto();

        query.setExpressions(Arrays.asList(new StringExpressionDto("x"), new StringExpressionDto("y"), new StringExpressionDto("count()")));

        final QueryDto union1 = new QueryDto();
        union1.setExpressions(Arrays.asList(new StringExpressionDto("x"), new StringExpressionDto("y")));
        union1.setFrom(new TableDto("report1"));

        final QueryDto union2 = new QueryDto();
        union2.setExpressions(Arrays.asList(new StringExpressionDto("a3"), new StringExpressionDto("b3")));
        union2.setFrom(new TableDto("reportA"));
        union2.setJoin(new JoinDto(
                JoinStrictness.ANY,
                JoinType.INNER,
                new TableDto("reportB"),
                Arrays.asList(new StringExpressionDto("a1"), new StringExpressionDto("a2")),
                Arrays.asList(new StringExpressionDto("b1"), new StringExpressionDto("b2"))
        ));

        final UnionAllDto unionAll = new UnionAllDto(Arrays.asList(union1, union2));
        query.setFrom(unionAll);

        final QueryDto subquery = new QueryDto();
        subquery.setDistinct(true);
        subquery.setExpressions(Arrays.asList(new StringExpressionDto("a"), new StringExpressionDto("b")));
        subquery.setFrom(new TableDto("report2"));
        query.setWhere(new AndDto(Arrays.asList(
                new BinaryComparisonFilterDto(new StringExpressionDto("x"), BinaryComparisonOperator.NOT_EQUALS, new StringExpressionDto("0")),
                new BinaryComparisonFilterDto(
                        new TupleDto(Arrays.asList(new StringExpressionDto("x"), new StringExpressionDto("y"))),
                        BinaryComparisonOperator.NOT_IN,
                        subquery
                ),
                new NotDto(new UnaryComparisonFilterDto(new StringExpressionDto("y"), UnaryComparisonOperator.IS_NOT_NULL))
        )));

        query.setGroupBy(Arrays.asList(new StringExpressionDto("x"), new StringExpressionDto("y")));
        query.setWithTotals(true);
        query.setHaving(new BinaryComparisonFilterDto(new StringExpressionDto("count()"), BinaryComparisonOperator.GREATER_OR_EQUALS, new StringExpressionDto("x")));
        query.setOrderBy(Arrays.asList(
                new SortDto(new StringExpressionDto("count()"), SortDirection.DESC),
                new SortDto(new StringExpressionDto("x"), SortDirection.ASC),
                new SortDto(new StringExpressionDto("y"), SortDirection.ASC)
        ));
        query.setLimitBy(new LimitByDto(Arrays.asList(new StringExpressionDto("x")), 10));
        query.setOffset(10L);
        query.setLimit(10L);

        final Column columnX = new ColumnImpl(Type.INT_32, "x");
        final Column columnY = new ColumnImpl(Type.INT_32, "y");
        final Table table1 = TableImpl.builder()
                .name("report1")
                .column("x", columnX)
                .column("y", columnY)
                .build();
        final Column columnA = new ColumnImpl(Type.INT_32, "a");
        final Column columnB = new ColumnImpl(Type.INT_32, "b");
        final Table table2 = TableImpl.builder()
                .name("report2")
                .column("a", columnA)
                .column("b", columnB)
                .build();
        final Column columnA1 = new ColumnImpl(Type.STRING, "a1");
        final Column columnA2 = new ColumnImpl(Type.TIMESTAMP, "a2");
        final Column columnA3 = new ColumnImpl(Type.INT_32, "a3");
        final Table tableA = TableImpl.builder()
                .name("reportA")
                .column("a1", columnA1)
                .column("a2", columnA2)
                .column("a3", columnA3)
                .build();
        final Column columnB1 = new ColumnImpl(Type.STRING, "b1");
        final Column columnB2 = new ColumnImpl(Type.TIMESTAMP, "b2");
        final Column columnB3 = new ColumnImpl(Type.INT_32, "b3");
        final Table tableB = TableImpl.builder()
                .name("reportB")
                .column("b1", columnB1)
                .column("b2", columnB2)
                .column("b3", columnB3)
                .build();

        final QueryImpl.QueryImplBuilder builder = QueryImpl.builder();

        final Expression count = new AggregationFunctionCallImpl(new FunctionImpl("count", Type.INT_64), Collections.emptyList(), Collections.emptyList());
        builder.expression(columnX);
        builder.expression(columnY);
        builder.expression(count);
        builder.from(UnionAllImpl.builder()
                .query(QueryImpl.builder()
                        .expression(columnX)
                        .expression(columnY)
                        .from(table1)
                        .build()
                ).query(QueryImpl.builder()
                        .expression(columnA3)
                        .expression(columnB3)
                        .from(tableA)
                        .join(JoinImpl.builder()
                                .strictness(JoinStrictness.ANY)
                                .type(JoinType.INNER)
                                .from(tableB)
                                .leftExpressions(List.of(columnA1, columnA2))
                                .rightExpressions(List.of(columnB1, columnB2))
                                .build()
                        ).build()
                ).build()
        );

        builder.where(AndImpl.builder()
                .filter(BinaryComparisonFilterImpl.of(columnX, BinaryComparisonOperator.NOT_EQUALS, NumberConstantImpl.valueOf(0L)))
                .filter(BinaryComparisonFilterImpl.of(
                        TupleImpl.of(Arrays.asList(columnX, columnY)),
                        BinaryComparisonOperator.NOT_IN,
                        QueryImpl.builder().distinct(true).expressions(List.of(columnA, columnB)).from(table2).build()
                ))
                .filter(NotImpl.of(
                        UnaryComparisonFilterImpl.of(columnY, UnaryComparisonOperator.IS_NOT_NULL)
                ))
                .build()
        );

        builder.groupBy(List.of(columnX, columnY));
        builder.withTotals(true);
        builder.having(BinaryComparisonFilterImpl.of(
                count,
                BinaryComparisonOperator.GREATER_OR_EQUALS,
                columnX
        ));
        builder.orderBy(List.of(
                SortImpl.of(count, SortDirection.DESC),
                SortImpl.of(columnX, SortDirection.ASC),
                SortImpl.of(columnY, SortDirection.ASC)
        ));
        builder.limitBy(LimitByImpl.of(Collections.singletonList(columnX), 10L));
        builder.offset(10L);
        builder.limit(10L);

        final Map<String, Table> reports = new HashMap<>();
        reports.put("reportA", tableA);
        reports.put("reportB", tableB);
        reports.put("report1", table1);
        reports.put("report2", table2);

        Assert.assertEquals(builder.build(), languageConverter.convert(query, reports));
    }
}
