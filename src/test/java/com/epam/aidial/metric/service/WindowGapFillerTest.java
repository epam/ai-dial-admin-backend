package com.epam.aidial.metric.service;

import com.epam.aidial.expressions.Expression;
import com.epam.aidial.expressions.enums.Type;
import com.epam.aidial.expressions.impl.AggregationFunctionCallImpl;
import com.epam.aidial.expressions.impl.AliasImpl;
import com.epam.aidial.expressions.impl.ColumnImpl;
import com.epam.aidial.expressions.impl.ConstantImpl;
import com.epam.aidial.expressions.impl.FunctionImpl;
import com.epam.aidial.expressions.impl.GroupFunctionCallImpl;
import com.epam.aidial.expressions.impl.NumberConstantImpl;
import com.epam.aidial.ql.common.model.enums.BinaryComparisonOperator;
import com.epam.aidial.ql.model.Query;
import com.epam.aidial.ql.model.filters.impl.AndImpl;
import com.epam.aidial.ql.model.filters.impl.BinaryComparisonFilterImpl;
import com.epam.aidial.ql.model.impl.QueryImpl;
import com.epam.aidial.ql.model.impl.TableImpl;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WindowGapFillerTest {

    private final WindowGapFiller gapFiller = new WindowGapFiller(10_000);

    @Nested
    class DefaultForTypeTests {

        @Test
        void floatReturnsZeroDouble() {
            assertThat(WindowGapFiller.defaultForType(Type.FLOAT)).isEqualTo(0.0);
        }

        @Test
        void doubleReturnsZeroDouble() {
            assertThat(WindowGapFiller.defaultForType(Type.DOUBLE)).isEqualTo(0.0);
        }

        @Test
        void integerReturnsZeroLong() {
            assertThat(WindowGapFiller.defaultForType(Type.INT_64)).isEqualTo(0L);
        }

        @Test
        void timestampReturnsZeroLong() {
            assertThat(WindowGapFiller.defaultForType(Type.TIMESTAMP)).isEqualTo(0L);
        }
    }

    @Nested
    class IsWindowQueryTests {

        @Test
        void returnsTrueWhenGroupByContainsWindowFunction() {
            var query = buildWindowQuery("1", "h",
                    Instant.parse("2026-03-11T00:00:00Z"),
                    Instant.parse("2026-03-12T00:00:00Z"));
            assertThat(WindowGapFiller.isWindowQuery(query)).isTrue();
        }

        @Test
        void returnsFalseWhenGroupByIsEmpty() {
            var query = QueryImpl.builder()
                    .expressions(List.of(countExpression()))
                    .from(TableImpl.builder().name("analytics").build())
                    .groupBy(List.of())
                    .build();
            assertThat(WindowGapFiller.isWindowQuery(query)).isFalse();
        }

        @Test
        void returnsFalseWhenGroupByIsNull() {
            var query = QueryImpl.builder()
                    .expressions(List.of(countExpression()))
                    .from(TableImpl.builder().name("analytics").build())
                    .build();
            assertThat(WindowGapFiller.isWindowQuery(query)).isFalse();
        }

        @Test
        void returnsFalseWhenGroupByHasOnlyColumns() {
            var query = QueryImpl.builder()
                    .expressions(List.of(new ColumnImpl(Type.STRING, "deployment"), countExpression()))
                    .from(TableImpl.builder().name("analytics").build())
                    .groupBy(List.of(new ColumnImpl(Type.STRING, "deployment")))
                    .build();
            assertThat(WindowGapFiller.isWindowQuery(query)).isFalse();
        }
    }

    @Nested
    class FillGapsTests {

        @Test
        void fillsGapsInWindowOnlyQuery() {
            // 3-hour window over 9 hours: expect 3 buckets
            var start = Instant.parse("2026-03-11T00:00:00Z");
            var end = Instant.parse("2026-03-11T09:00:00Z");
            var query = buildWindowQuery("3", "h", start, end);

            // Only provide data for first and last bucket
            var rows = new ArrayList<List<Object>>();
            rows.add(new ArrayList<>(List.of(Instant.parse("2026-03-11T00:00:00Z"), 5L)));
            rows.add(new ArrayList<>(List.of(Instant.parse("2026-03-11T06:00:00Z"), 3L)));

            var result = gapFiller.fillGaps(rows, query);

            assertThat(result).containsExactly(
                    List.of(Instant.parse("2026-03-11T00:00:00Z"), 5L),
                    List.of(Instant.parse("2026-03-11T03:00:00Z"), 0L),
                    List.of(Instant.parse("2026-03-11T06:00:00Z"), 3L)
            );
        }

        @Test
        void fillsGapsInWindowAndColumnQuery() {
            // 1-day window over 2 days with deployment column
            var start = Instant.parse("2026-03-11T00:00:00Z");
            var end = Instant.parse("2026-03-13T00:00:00Z");
            var query = buildWindowColumnQuery("1", "d", start, end);

            // day1: only gpt-4, day2: only gpt-3.5
            var rows = new ArrayList<List<Object>>();
            rows.add(new ArrayList<>(List.of(Instant.parse("2026-03-11T00:00:00Z"), "gpt-4", 2L)));
            rows.add(new ArrayList<>(List.of(Instant.parse("2026-03-12T00:00:00Z"), "gpt-3.5", 1L)));

            var result = gapFiller.fillGaps(rows, query);

            assertThat(result).containsExactlyInAnyOrder(
                    List.of(Instant.parse("2026-03-11T00:00:00Z"), "gpt-4", 2L),
                    List.of(Instant.parse("2026-03-11T00:00:00Z"), "gpt-3.5", 0L),
                    List.of(Instant.parse("2026-03-12T00:00:00Z"), "gpt-4", 0L),
                    List.of(Instant.parse("2026-03-12T00:00:00Z"), "gpt-3.5", 1L)
            );
        }

        @Test
        void returnsEmptyWhenNoDataAndGroupColumnsPresent() {
            var start = Instant.parse("2026-03-11T00:00:00Z");
            var end = Instant.parse("2026-03-13T00:00:00Z");
            var query = buildWindowColumnQuery("1", "d", start, end);

            var result = gapFiller.fillGaps(List.of(), query);

            assertThat(result).isEmpty();
        }

        @Test
        void returnsRowsUnchangedWhenNoWindowFunction() {
            var query = QueryImpl.builder()
                    .expressions(List.of(new ColumnImpl(Type.STRING, "deployment"), countExpression()))
                    .from(TableImpl.builder().name("analytics").build())
                    .groupBy(List.of(new ColumnImpl(Type.STRING, "deployment")))
                    .where(timeRangeFilter(
                            Instant.parse("2026-03-11T00:00:00Z"),
                            Instant.parse("2026-03-12T00:00:00Z")))
                    .build();

            var rows = List.<List<Object>>of(
                    List.of("gpt-4", 5L),
                    List.of("gpt-3.5", 3L)
            );

            var result = gapFiller.fillGaps(rows, query);

            assertThat(result).isEqualTo(rows);
        }

        @Test
        void handlesAliasedWindowExpression() {
            var start = Instant.parse("2026-03-11T00:00:00Z");
            var end = Instant.parse("2026-03-11T06:00:00Z");

            var windowFunc = windowFunction("3", "h");
            var aliasedWindow = new AliasImpl("time", windowFunc);

            var query = QueryImpl.builder()
                    .expressions(List.of(aliasedWindow, countExpression()))
                    .from(TableImpl.builder().name("analytics").build())
                    .groupBy(List.of(windowFunc))
                    .where(timeRangeFilter(start, end))
                    .build();

            // Only first bucket has data
            var rows = new ArrayList<List<Object>>();
            rows.add(new ArrayList<>(List.of(Instant.parse("2026-03-11T00:00:00Z"), 1L)));

            var result = gapFiller.fillGaps(rows, query);

            assertThat(result).containsExactly(
                    List.of(Instant.parse("2026-03-11T00:00:00Z"), 1L),
                    List.of(Instant.parse("2026-03-11T03:00:00Z"), 0L)
            );
        }

        @Test
        void fillsZeroDoubleForFloatAggregations() {
            var start = Instant.parse("2026-03-11T00:00:00Z");
            var end = Instant.parse("2026-03-11T06:00:00Z");
            var windowFunc = windowFunction("3", "h");

            var sumExpr = new AggregationFunctionCallImpl(
                    new FunctionImpl("sum", Type.DOUBLE, true),
                    List.of(),
                    List.of(new ColumnImpl(Type.DOUBLE, "price"))
            );

            var query = QueryImpl.builder()
                    .expressions(List.of(windowFunc, sumExpr))
                    .from(TableImpl.builder().name("analytics").build())
                    .groupBy(List.of(windowFunc))
                    .where(timeRangeFilter(start, end))
                    .build();

            var rows = new ArrayList<List<Object>>();
            rows.add(new ArrayList<>(List.of(Instant.parse("2026-03-11T00:00:00Z"), 5.0)));

            var result = gapFiller.fillGaps(rows, query);

            assertThat(result).containsExactly(
                    List.of(Instant.parse("2026-03-11T00:00:00Z"), 5.0),
                    List.of(Instant.parse("2026-03-11T03:00:00Z"), 0.0)
            );
        }
    }

    @Nested
    class BucketLimitTests {

        @Test
        void throwsWhenTooManyBuckets() {
            // 1-second window over a range that would produce > 10,000 buckets
            var start = Instant.parse("2026-03-11T00:00:00Z");
            var end = Instant.parse("2026-03-11T04:00:00Z"); // 14400 seconds > 10000
            var query = buildWindowQuery("1", "s", start, end);

            assertThatThrownBy(() -> gapFiller.fillGaps(List.of(), query))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("too many buckets");
        }

        @Test
        void respectsCustomMaxBuckets() {
            var smallLimitFiller = new WindowGapFiller(5);
            // 1-hour window over 6 hours = 6 buckets > 5
            var start = Instant.parse("2026-03-11T00:00:00Z");
            var end = Instant.parse("2026-03-11T06:00:00Z");
            var query = buildWindowQuery("1", "h", start, end);

            assertThatThrownBy(() -> smallLimitFiller.fillGaps(List.of(), query))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("too many buckets");
        }
    }

    @Nested
    class BucketAlignmentTests {

        @Test
        void hourlyBucketsAlignToEpoch() {
            // Start at 13:33, 8-hour buckets should align to 08:00
            var start = Instant.parse("2026-03-11T13:33:00Z");
            var end = Instant.parse("2026-03-12T01:00:00Z");
            var query = buildWindowQuery("8", "h", start, end);

            var result = gapFiller.fillGaps(List.of(), query);

            assertThat(result).extracting(row -> row.get(0)).containsExactly(
                    Instant.parse("2026-03-11T08:00:00Z"),
                    Instant.parse("2026-03-11T16:00:00Z"),
                    Instant.parse("2026-03-12T00:00:00Z")
            );
        }

        @Test
        void dailyBucketsAlignToMidnight() {
            var start = Instant.parse("2026-03-11T14:00:00Z");
            var end = Instant.parse("2026-03-13T10:00:00Z");
            var query = buildWindowQuery("1", "d", start, end);

            var result = gapFiller.fillGaps(List.of(), query);

            assertThat(result).extracting(row -> row.get(0)).containsExactly(
                    Instant.parse("2026-03-11T00:00:00Z"),
                    Instant.parse("2026-03-12T00:00:00Z"),
                    Instant.parse("2026-03-13T00:00:00Z")
            );
        }

        @Test
        void monthlyBucketsAlignToFirstOfMonth() {
            var start = Instant.parse("2026-03-15T00:00:00Z");
            var end = Instant.parse("2026-06-01T00:00:00Z");
            var query = buildWindowQuery("1", "mo", start, end);

            var result = gapFiller.fillGaps(List.of(), query);

            assertThat(result).extracting(row -> row.get(0)).containsExactly(
                    Instant.parse("2026-03-01T00:00:00Z"),
                    Instant.parse("2026-04-01T00:00:00Z"),
                    Instant.parse("2026-05-01T00:00:00Z")
            );
        }
    }

    // -- Helper methods --

    private static GroupFunctionCallImpl windowFunction(String value, String unit) {
        return new GroupFunctionCallImpl(
                new FunctionImpl("window", null, true),
                List.of(),
                List.of(
                        new ColumnImpl(Type.TIMESTAMP, "_time"),
                        NumberConstantImpl.valueOf(Long.parseLong(value)),
                        new ConstantImpl(Type.STRING, unit)
                )
        );
    }

    private static Expression countExpression() {
        return new AggregationFunctionCallImpl(
                new FunctionImpl("count", Type.UINT_64, true),
                List.of(),
                List.of()
        );
    }

    private static AndImpl timeRangeFilter(Instant start, Instant end) {
        return AndImpl.of(List.of(
                BinaryComparisonFilterImpl.of(
                        new ColumnImpl(Type.TIMESTAMP, "_time"),
                        BinaryComparisonOperator.GREATER_OR_EQUALS,
                        new ConstantImpl(Type.TIMESTAMP, start.toEpochMilli())),
                BinaryComparisonFilterImpl.of(
                        new ColumnImpl(Type.TIMESTAMP, "_time"),
                        BinaryComparisonOperator.LESS,
                        new ConstantImpl(Type.TIMESTAMP, end.toEpochMilli()))
        ));
    }

    private static Query buildWindowQuery(String value, String unit, Instant start, Instant end) {
        var windowFunc = windowFunction(value, unit);
        return QueryImpl.builder()
                .expressions(List.of(windowFunc, countExpression()))
                .from(TableImpl.builder().name("analytics").build())
                .groupBy(List.of(windowFunc))
                .where(timeRangeFilter(start, end))
                .build();
    }

    private static Query buildWindowColumnQuery(String value, String unit, Instant start, Instant end) {
        var windowFunc = windowFunction(value, unit);
        var deploymentCol = new ColumnImpl(Type.STRING, "deployment");
        return QueryImpl.builder()
                .expressions(List.of(windowFunc, deploymentCol, countExpression()))
                .from(TableImpl.builder().name("analytics").build())
                .groupBy(List.of(windowFunc, deploymentCol))
                .where(timeRangeFilter(start, end))
                .build();
    }
}
