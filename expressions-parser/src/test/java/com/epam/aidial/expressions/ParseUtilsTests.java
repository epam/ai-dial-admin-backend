package com.epam.aidial.expressions;

import com.epam.aidial.expressions.enums.Type;
import com.epam.aidial.expressions.exceptions.ParseException;
import com.epam.aidial.expressions.impl.AggregationFunctionCallImpl;
import com.epam.aidial.expressions.impl.AliasImpl;
import com.epam.aidial.expressions.impl.ColumnImpl;
import com.epam.aidial.expressions.impl.ConstantImpl;
import com.epam.aidial.expressions.impl.DummyFunctionDatasourceImpl;
import com.epam.aidial.expressions.impl.FunctionCallImpl;
import com.epam.aidial.expressions.impl.FunctionImpl;
import com.epam.aidial.expressions.impl.NumberConstantImpl;
import com.epam.aidial.expressions.impl.ScaleCallImpl;
import com.epam.aidial.expressions.impl.ValidRequestedExpressionImpl;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ParseUtilsTests {
    private static final FunctionsDatasource functionDatasource = new DummyFunctionDatasourceImpl();

    public void testException(String expression, Map<String, Column> map) {
        try {
            final ValidRequestedExpression actual = ParseUtils.parseExpression(expression, functionDatasource, map);
            Assert.fail(String.format("Should throw exception on '%s', but parsed as '%s'", expression, actual));
        } catch (ParseException e) {
        }
    }

    @Test(timeout = 5000L)
    public void shouldThrowExceptionOnIncorrectSyntax() {
        testException("$", Collections.emptyMap());
        testException("count(", Collections.emptyMap());
        testException("test)", Collections.emptyMap());
        testException("test", Collections.emptyMap());
        testException("avg(string)", Collections.singletonMap("string", new ColumnImpl(Type.STRING, "string")));
        testException("avg(int, int)", Collections.singletonMap("int", new ColumnImpl(Type.INT_64, "int")));
        testException("quantile(1)(int)", Collections.singletonMap("int", new ColumnImpl(Type.INT_64, "int")));
        testException("quantile(1.1, int)", Collections.singletonMap("int", new ColumnImpl(Type.INT_64, "int")));
        testException("quantile(1.0, 2.0)(int)", Collections.singletonMap("int", new ColumnImpl(Type.INT_64, "int")));
        testException("abs(1.0, -2.0)", Collections.emptyMap());
        testException("1 + ''", Collections.emptyMap());
        testException("1 < 2 <= 3", Collections.emptyMap());
        testException("1 ? 2 : 3 ? 4 : 5", Collections.emptyMap());
        testException("--1", Collections.emptyMap());
        testException("--(2 * 2)", Collections.emptyMap());
        testException("1---1", Collections.emptyMap());
        testException("1+++1", Collections.emptyMap());
        testException("round(int, int)", Collections.singletonMap("int", new ColumnImpl(Type.INT_64, "int")));
        testException("count() + int", Collections.singletonMap("int", new ColumnImpl(Type.INT_64, "int")));
        testException("sum(count())", Collections.emptyMap());
        testException("toIntervalStart(toIntervalNumber(1, 1), 1, 1)", Collections.emptyMap());
        testException("1 AS x", Collections.emptyMap());
    }

    private Constant toConstant(Comparable constant) {
        if (constant == null) {
            return ConstantImpl.NULL;
        }
        if (constant instanceof Long) {
            return NumberConstantImpl.valueOf((Long) constant);
        } else if (constant instanceof Double) {
            return NumberConstantImpl.valueOf((Double) constant);
        } else if (constant instanceof String) {
            return new ConstantImpl(Type.STRING, constant);
        } else if (constant instanceof Boolean) {
            return new ConstantImpl(Type.BOOLEAN, constant);
        }
        throw new IllegalStateException();
    }

    private void testConstant(Comparable constant, String string) {
        final ValidRequestedExpression expected = new ValidRequestedExpressionImpl(string, toConstant(constant));
        final ValidRequestedExpression actual = ParseUtils.parseExpression(string, functionDatasource, Collections.emptyMap());
        Assert.assertEquals(expected, actual);
    }

    @Test(timeout = 5000L)
    public void shouldParseConstants() {
        testConstant(0L, "0");
        testConstant(1234567890L, "1234567890");
        testConstant(9223372036854776000D, "9223372036854776000");
        testConstant(-1234567890L, "-1234567890");
        testConstant(1234567890L, "+1234567890");
        testConstant(0.123456789, "0.123456789");
        testConstant(-0.123456789, "-0.123456789");
        testConstant(0.123456789, "+0.123456789");
        testConstant(1.2345678e9, "1.2345678e9");
        testConstant(8.07654321e-9, "8.07654321e-9");
        testConstant(8.07654321e9, "8.07654321e+9");
        testConstant(0x123456789L, "0x123456789");
        testConstant("string constant", "'string constant'");
        testConstant("", "''");
        testConstant(true, "true");
        testConstant(false, "false");
        testConstant(true, "TRUE");
        testConstant(true, "tRuE");
        testConstant(false, "FaLSe");
        testConstant(Double.NaN, "nan");
        testConstant(Double.POSITIVE_INFINITY, "Inf");
        testConstant(Double.NEGATIVE_INFINITY, "-InFinItY");
        testConstant(null, "null");
        testConstant(null, "NuLL");
    }

    private void testSingleOperator(FunctionInfo function, Comparable constant, String string) {
        final ValidRequestedExpression expected = new ValidRequestedExpressionImpl(string, new FunctionCallImpl(function, toConstant(constant)));
        final ValidRequestedExpression actual = ParseUtils.parseExpression(string, functionDatasource, Collections.emptyMap());
        Assert.assertEquals(expected, actual);
    }

    @Test(timeout = 5000L)
    public void shouldParseNegateOperator() {
        testSingleOperator(new FunctionImpl("negate", Type.INT_32), 0L, "-(0)");
        testSingleOperator(new FunctionImpl("negate", Type.INT_32), 432L, "-(432)");
        testSingleOperator(new FunctionImpl("negate", Type.DOUBLE), 123., "-(123.0)");
        testSingleOperator(new FunctionImpl("negate", Type.DOUBLE), 123e5, "-(123e5)");
        testSingleOperator(new FunctionImpl("negate", Type.DOUBLE), Double.POSITIVE_INFINITY, "-(inf)");
    }

    @Test(timeout = 5000L)
    public void shouldParseNegateExpression() {
        final String string = "-(2 * 2)";
        final ValidRequestedExpression expected = new ValidRequestedExpressionImpl(
                string,
                new FunctionCallImpl(
                        new FunctionImpl("negate", Type.INT_32),
                        new FunctionCallImpl(new FunctionImpl("multiply", Type.INT_32), toConstant(2L), toConstant(2L))
                )
        );
        final ValidRequestedExpression actual = ParseUtils.parseExpression(string, functionDatasource, Collections.emptyMap());
        Assert.assertEquals(expected, actual);
    }

    private void testDualOperator(Comparable a, FunctionInfo function, Comparable b, String string) {
        final ValidRequestedExpression expected = new ValidRequestedExpressionImpl(string, new FunctionCallImpl(function, toConstant(a), toConstant(b)));
        final ValidRequestedExpression actual = ParseUtils.parseExpression(string, functionDatasource, Collections.emptyMap());
        Assert.assertEquals(expected, actual);
    }

    @Test(timeout = 5000L)
    public void shouldArithmeticOperator() {
        testDualOperator(1L, new FunctionImpl("plus", Type.INT_32), -3L, "1 + -3");
        testDualOperator(5L, new FunctionImpl("modulo", Type.INT_32), 10L, "5 % 10");
        testDualOperator(1L, new FunctionImpl("plus", Type.INT_32), -3L, "1+-3");
        testDualOperator(1L, new FunctionImpl("minus", Type.INT_32), -3L, "1--3");
        testDualOperator(1L, new FunctionImpl("plus", Type.INT_32), 3L, "1++3");
        testDualOperator(1L, new FunctionImpl("plus", Type.INT_32), null, "1+null");
    }

    @Test(timeout = 5000L)
    public void shouldCompare() {
        testDualOperator(1L, new FunctionImpl("equals", Type.BOOLEAN), -3L, "1 == -3");
        testDualOperator(5L, new FunctionImpl("greater", Type.BOOLEAN), 10L, "5 > 10");
        testDualOperator(1L, new FunctionImpl("notEquals", Type.BOOLEAN), -3L, "1 != -3");
        testDualOperator(5L, new FunctionImpl("less", Type.BOOLEAN), 10L, "5 < 10");
        testDualOperator(1L, new FunctionImpl("greaterOrEquals", Type.BOOLEAN), -3L, "1 >= -3");
        testDualOperator(5L, new FunctionImpl("lessOrEquals", Type.BOOLEAN), 10L, "5 <= 10");
        testDualOperator(5L, new FunctionImpl("lessOrEquals", Type.BOOLEAN), null, "5 <= null");
    }

    @Test(timeout = 5000L)
    public void testLogicalOperators() {
        testDualOperator(true, new FunctionImpl("and", Type.BOOLEAN), false, "true AND false");
        testDualOperator(false, new FunctionImpl("or", Type.BOOLEAN), true, "false OR true");
        testDualOperator(null, new FunctionImpl("or", Type.BOOLEAN), true, "null OR true");
        testSingleOperator(new FunctionImpl("not", Type.BOOLEAN), true, "NOT true");
        testSingleOperator(new FunctionImpl("not", Type.BOOLEAN), null, "NOT null");
    }

    @Test(timeout = 5000L)
    public void TestTernaryOperator() {
        final String expression = "1 > 2 ? (true ? 1 : 2) : 2";
        final ValidRequestedExpression expected = new ValidRequestedExpressionImpl(
                expression,
                new FunctionCallImpl(
                        new FunctionImpl("if", Type.INT_32),
                        new FunctionCallImpl(new FunctionImpl("greater", Type.BOOLEAN), toConstant(1L), toConstant(2L)),
                        new FunctionCallImpl(new FunctionImpl("if", Type.INT_32), toConstant(true), toConstant(1L), toConstant(2L)),
                        toConstant(2L)
                )
        );
        final ValidRequestedExpression actual = ParseUtils.parseExpression(expression, functionDatasource, Collections.emptyMap());
        Assert.assertEquals(expected, actual);
    }

    @Test(timeout = 5000L)
    public void testPriority() {
        final String expression = "(true OR NOT 1 * (5 - 3) <= 1 + 2 % 3 + (4 * 5 - 6) / 7 / 8) OR 1 > 5 AND NOT false";

        final Expression left =
                new FunctionCallImpl(
                        new FunctionImpl("multiply", Type.INT_32),
                        toConstant(1L),
                        new FunctionCallImpl(new FunctionImpl("minus", Type.INT_32), toConstant(5L), toConstant(3L))
                );

        final Expression right =
                new FunctionCallImpl(
                        new FunctionImpl("plus", Type.DOUBLE),
                        new FunctionCallImpl(
                                new FunctionImpl("plus", Type.INT_32),
                                toConstant(1L),
                                new FunctionCallImpl(new FunctionImpl("modulo", Type.INT_32), toConstant(2L), toConstant(3L))
                        ),
                        new FunctionCallImpl(
                                new FunctionImpl("divide", Type.DOUBLE),
                                new FunctionCallImpl(
                                        new FunctionImpl("divide", Type.DOUBLE),
                                        new FunctionCallImpl(
                                                new FunctionImpl("minus", Type.INT_32),
                                                new FunctionCallImpl(new FunctionImpl("multiply", Type.INT_32), toConstant(4L), toConstant(5L)),
                                                toConstant(6L)
                                        ),
                                        toConstant(7L)
                                ),
                                toConstant(8L)
                        )
                );

        final ValidRequestedExpression expected = new ValidRequestedExpressionImpl(
                expression,
                new FunctionCallImpl(
                        new FunctionImpl("or", Type.BOOLEAN),
                        new FunctionCallImpl(
                                new FunctionImpl("or", Type.BOOLEAN),
                                toConstant(true),
                                new FunctionCallImpl(
                                        new FunctionImpl("not", Type.BOOLEAN),
                                        new FunctionCallImpl(new FunctionImpl("lessOrEquals", Type.BOOLEAN), left, right)
                                )
                        ),
                        new FunctionCallImpl(
                                new FunctionImpl("and", Type.BOOLEAN),
                                new FunctionCallImpl(new FunctionImpl("greater", Type.BOOLEAN), toConstant(1L), toConstant(5L)),
                                new FunctionCallImpl(new FunctionImpl("not", Type.BOOLEAN), toConstant(false))
                        )
                )
        );
        final ValidRequestedExpression actual = ParseUtils.parseExpression(expression, functionDatasource, Collections.emptyMap());

        Assert.assertEquals(expected, actual);
    }

    private void testColumnName(String columnName) {
        final ColumnImpl column = new ColumnImpl(Type.STRING, columnName);
        final ValidRequestedExpression expected = new ValidRequestedExpressionImpl(columnName, column);
        final ValidRequestedExpression actual = ParseUtils.parseExpression(columnName, functionDatasource, Collections.singletonMap(columnName, column));
        Assert.assertEquals(expected, actual);
    }

    @Test(timeout = 5000L)
    public void shouldParseColumnName() {
        testColumnName("__ID__");
        testColumnName("column");
    }

    private void testAggregationFunction(FunctionInfo function, List<? extends Comparable> constants, List<Expression> exceptions, String expression, Map<String, Column> map) {
        final List<Constant> constantList = constants.stream().map(this::toConstant).collect(Collectors.toList());
        final ValidRequestedExpression expected = new ValidRequestedExpressionImpl(expression, new AggregationFunctionCallImpl(function, constantList, exceptions));
        final ValidRequestedExpression actual = ParseUtils.parseExpression(expression, functionDatasource, map);
        Assert.assertEquals(expected, actual);
    }

    @Test(timeout = 5000L)
    public void shouldParseAggregationFunction() {
        testAggregationFunction(new FunctionImpl("count", Type.INT_64), Collections.emptyList(), Collections.emptyList(), "count()", Collections.emptyMap());
        testAggregationFunction(new FunctionImpl("sum", Type.INT_64), Collections.emptyList(), Collections.singletonList(toConstant(1L)), "sum(1)", Collections.emptyMap());
        testAggregationFunction(new FunctionImpl("sum", Type.INT_64), Collections.emptyList(), Collections.singletonList(ConstantImpl.NULL), "sum(null)", Collections.emptyMap());
        {
            final Column column = new ColumnImpl(Type.FLOAT, "column");
            testAggregationFunction(new FunctionImpl("quantile", Type.DOUBLE, false), Collections.singletonList(0.95), Collections.singletonList(column), "quantile(0.95, column)", Collections.singletonMap(column.getName(), column));
        }
    }

    @Test(timeout = 5000L)
    public void shouldParseFunctionsInAggregationFunction() {
        final String expression = "1 + sum(a + abs(b))";
        final ValidRequestedExpression expected = new ValidRequestedExpressionImpl(expression, new FunctionCallImpl(
                new FunctionImpl("plus", Type.INT_64),
                toConstant(1L),
                new AggregationFunctionCallImpl(
                        new FunctionImpl("sum", Type.INT_64),
                        Collections.emptyList(),
                        Collections.singletonList(new FunctionCallImpl(
                                new FunctionImpl("plus", Type.INT_64),
                                new ColumnImpl(Type.INT_64, "a"),
                                new FunctionCallImpl(
                                        new FunctionImpl("abs", Type.INT_64),
                                        new ColumnImpl(Type.INT_64, "b")
                                )
                        ))
                )
        ));

        final Map<String, Column> map = new HashMap<>();
        map.put("a", new ColumnImpl(Type.INT_64, "a"));
        map.put("b", new ColumnImpl(Type.INT_64, "b"));

        final ValidRequestedExpression actual = ParseUtils.parseExpression(expression, functionDatasource, map);
        Assert.assertEquals(expected, actual);
    }

    private void testFunction(FunctionInfo function, List<Expression> exceptions, String expression, Map<String, Column> map) {
        final ValidRequestedExpression expected = new ValidRequestedExpressionImpl(expression, new FunctionCallImpl(function, exceptions));
        final ValidRequestedExpression actual = ParseUtils.parseExpression(expression, functionDatasource, map);
        Assert.assertEquals(expected, actual);
    }

    @Test(timeout = 5000L)
    public void shouldParseFunction() {
        testFunction(new FunctionImpl("abs", Type.INT_32), Collections.singletonList(toConstant(-1L)), "abs(-1)", Collections.emptyMap());
        {
            final Column column = new ColumnImpl(Type.FLOAT, "column");
            testFunction(new FunctionImpl("abs", Type.FLOAT), Collections.singletonList(column), "abs(column)", Collections.singletonMap(column.getName(), column));
        }
        {
            final Column column = new ColumnImpl(Type.FLOAT, "column");
            testFunction(new FunctionImpl("round", Type.DOUBLE), Arrays.asList(column, toConstant(2L)), "round(column, 2)", Collections.singletonMap(column.getName(), column));
        }
    }

    @Test(timeout = 5000L)
    public void shouldParseScaleCall() {
        final String expression = "intervalScaleNumber(1, 1)";
        final ValidRequestedExpression actual = ParseUtils.parseExpression(expression, functionDatasource, Collections.emptyMap());
        final ValidRequestedExpression expected = new ValidRequestedExpressionImpl(expression, new ScaleCallImpl(
                new FunctionImpl("intervalScaleNumber", Type.INT_32),
                toConstant(1L),
                toConstant(1L)
        ));
        Assert.assertEquals(expected, actual);
    }

    @Test(timeout = 5000L)
    public void shouldParseWithAlias() {
        final List<String> expressions = Arrays.asList("x + y", "1 AS x", "x * 5 AS y");
        final Pair<List<ValidRequestedExpression>, Map<String, ? extends Column>> actual =
                ParseUtils.parseExpressions(expressions, functionDatasource, Collections.emptyMap(), true);

        final Alias x = new AliasImpl("x", toConstant(1L));
        final Alias y = new AliasImpl("y", new FunctionCallImpl(new FunctionImpl("multiply", Type.INT_32), x, toConstant(5L)));
        final List<ValidRequestedExpression> expectedExpressions = Arrays.asList(
                new ValidRequestedExpressionImpl("x + y", new FunctionCallImpl(new FunctionImpl("plus", Type.INT_32), x, y)),
                new ValidRequestedExpressionImpl("1 AS x", x),
                new ValidRequestedExpressionImpl("x * 5 AS y", y)
        );
        final Map<String, Column> map = new HashMap<>();
        map.put("x", x);
        map.put("y", y);
        final Pair<List<ValidRequestedExpression>, Map<String, ? extends Column>> expected = Pair.of(expectedExpressions, map);

        Assert.assertEquals(expected, actual);
    }

    public void testExceptions(Map<String, Column> columns, String... expressions) {
        try {
            final List<ValidRequestedExpression> actual = ParseUtils.parseExpressions(Arrays.asList(expressions), functionDatasource, columns, true).getLeft();
            Assert.fail(String.format("Should throw exception on '%s', but parsed as '%s'", Arrays.asList(expressions), actual));
        } catch (ParseException e) {
        }
    }

    @Test(timeout = 5000L)
    public void shouldThrowExceptionOnAliases() {
        testExceptions(Collections.emptyMap(), "1 AS x", "2 AS x");
        testExceptions(Collections.emptyMap(), "count() AS cnt", "avg(cnt)");
        testExceptions(Collections.singletonMap("x", new ColumnImpl(Type.INT_32, "x")), "count() AS cnt", "2 AS x", "x + cnt");
    }
}
