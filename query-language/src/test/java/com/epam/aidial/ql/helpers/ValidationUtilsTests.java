package com.epam.aidial.ql.helpers;

import com.epam.aidial.expressions.Column;
import com.epam.aidial.expressions.Expression;
import com.epam.aidial.expressions.enums.Type;
import com.epam.aidial.expressions.impl.ColumnImpl;
import com.epam.aidial.expressions.impl.ConstantImpl;
import com.epam.aidial.ql.common.model.enums.BinaryComparisonOperator;
import com.epam.aidial.ql.common.model.enums.DenialCause;
import com.epam.aidial.ql.exceptions.ValidationException;
import com.epam.aidial.ql.model.impl.QueryImpl;
import com.epam.aidial.ql.model.impl.TableImpl;
import com.epam.aidial.ql.model.impl.TupleImpl;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ValidationUtilsTests {

    @Test
    public void shouldVerifySupportOperatorSimpleConstants() {
        ValidationUtils.verifySupportOperator(
                BinaryComparisonOperator.GREATER_OR_EQUALS,
                new ConstantImpl(Type.INT_32, 1),
                new ConstantImpl(Type.INT_64, 2),
                "1",
                "2"
        );
    }

    @Test
    public void shouldVerifySupportOperatorSimpleExpressions() {
        ValidationUtils.verifySupportOperator(
                BinaryComparisonOperator.EQUALS,
                new ColumnImpl(Type.DOUBLE, "column"),
                new ConstantImpl(Type.INT_64, 2),
                "column",
                "2"
        );
    }

    @Test(expected = ValidationException.class)
    public void shouldNotVerifySupportOperatorDoubleIn() {
        ValidationUtils.verifySupportOperator(
                BinaryComparisonOperator.IN,
                new ColumnImpl(Type.DOUBLE, "column"),
                new ConstantImpl(Type.INT_64, 2),
                "column",
                "2"
        );
    }

    @Test
    public void shouldVerifySupportOperatorInTuple() {
        ValidationUtils.verifySupportOperator(
                BinaryComparisonOperator.IN,
                new ColumnImpl(Type.INT_64, "column"),
                TupleImpl.of(Arrays.asList(new ConstantImpl(Type.INT_64, 2), new ConstantImpl(Type.INT_32, 3))),
                "column",
                "subquery"
        );
    }

    @Test
    public void shouldVerifySupportOperatorInSubquery() {
        ValidationUtils.verifySupportOperator(
                BinaryComparisonOperator.IN,
                new ColumnImpl(Type.INT_64, "column"),
                QueryImpl.builder()
                        .expression(new ConstantImpl(Type.INT_8, 1))
                        .from(TableImpl.builder().name("report").build())
                        .build(),
                "column",
                "subquery"
        );
    }

    @Test
    public void shouldVerifySupportOperatorStringColumnEqualsUuidLiteral() {
        ValidationUtils.verifySupportOperator(
                BinaryComparisonOperator.EQUALS,
                new ColumnImpl(Type.STRING, "project_id"),
                new ConstantImpl(Type.UUID, UUID.fromString("a36d8a75-aa7d-4185-a84d-566066cf91f2")),
                "project_id",
                "'a36d8a75-aa7d-4185-a84d-566066cf91f2'"
        );
    }

    @Test
    public void shouldVerifySupportOperatorUuidColumnEqualsStringLiteral() {
        ValidationUtils.verifySupportOperator(
                BinaryComparisonOperator.EQUALS,
                new ColumnImpl(Type.UUID, "id"),
                new ConstantImpl(Type.STRING, "a36d8a75-aa7d-4185-a84d-566066cf91f2"),
                "id",
                "'a36d8a75-aa7d-4185-a84d-566066cf91f2'"
        );
    }

    @Test
    public void shouldVerifySupportOperatorStringColumnInUuidTuple() {
        ValidationUtils.verifySupportOperator(
                BinaryComparisonOperator.IN,
                new ColumnImpl(Type.STRING, "project_id"),
                TupleImpl.of(Arrays.asList(
                        new ConstantImpl(Type.UUID, UUID.fromString("a36d8a75-aa7d-4185-a84d-566066cf91f2")),
                        new ConstantImpl(Type.UUID, UUID.fromString("b47e9b86-bb8e-5296-b95e-677177d02e93"))
                )),
                "project_id",
                "uuids"
        );
    }

    @Test
    public void shouldVerifySupportOperatorStringColumnNotEqualsUuidLiteral() {
        ValidationUtils.verifySupportOperator(
                BinaryComparisonOperator.NOT_EQUALS,
                new ColumnImpl(Type.STRING, "project_id"),
                new ConstantImpl(Type.UUID, UUID.fromString("a36d8a75-aa7d-4185-a84d-566066cf91f2")),
                "project_id",
                "'a36d8a75-aa7d-4185-a84d-566066cf91f2'"
        );
    }

    @Test(expected = ValidationException.class)
    public void shouldNotVerifySupportOperatorUuidLessThanUuid() {
        ValidationUtils.verifySupportOperator(
                BinaryComparisonOperator.LESS,
                new ColumnImpl(Type.UUID, "id"),
                new ConstantImpl(Type.UUID, UUID.fromString("a36d8a75-aa7d-4185-a84d-566066cf91f2")),
                "id",
                "'a36d8a75-aa7d-4185-a84d-566066cf91f2'"
        );
    }

    @Test
    public void shouldVerifySupportOperatorEqualSubqueries() {
        ValidationUtils.verifySupportOperator(
                BinaryComparisonOperator.GREATER,
                QueryImpl.builder()
                        .expression(new ConstantImpl(Type.INT_32, 1))
                        .from(TableImpl.builder().name("report").build())
                        .build(),
                QueryImpl.builder()
                        .expression(new ConstantImpl(Type.INT_8, 1))
                        .from(TableImpl.builder().name("report").build())
                        .build(),
                "column",
                "subquery"
        );
    }

    private static Expression TAG = new Expression() {
        @Override
        public Type getType() {
            return Type.TAG;
        }

        @Override
        public boolean isAggregation() {
            return false;
        }

        @Override
        public boolean isScale() {
            return false;
        }

        @Override
        public boolean isDeterministic() {
            return true;
        }

        @Override
        public Set<Column> getDependentColumns() {
            return Collections.emptySet();
        }
    };
    private static Expression COLUMN = new ColumnImpl(Type.INT_32, "column");
    private static Expression EXPRESSION = new Expression() {
        @Override
        public Type getType() {
            return Type.DOUBLE;
        }

        @Override
        public boolean isAggregation() {
            return true;
        }

        @Override
        public boolean isScale() {
            return true;
        }

        @Override
        public boolean isDeterministic() {
            return false;
        }

        @Override
        public Set<Column> getDependentColumns() {
            return Collections.emptySet();
        }
    };

    @Test
    public void testAllowExpressionsByColumn() {
        Assert.assertEquals(List.of(), ValidationUtils.isAllowExpressions(COLUMN));
    }

    @Test
    public void testAllowExpressionsByTag() {
        Assert.assertEquals(List.of(DenialCause.TYPE), ValidationUtils.isAllowExpressions(TAG));
    }

    @Test
    public void testAllowExpressionsByExpression() {
        Assert.assertEquals(List.of(), ValidationUtils.isAllowExpressions(EXPRESSION));
    }

    @Test
    public void testAllowAggregationByColumn() {
        Assert.assertEquals(List.of(), ValidationUtils.isAllowAggregation(COLUMN));
    }

    @Test
    public void testAllowAggregationByTag() {
        Assert.assertEquals(List.of(DenialCause.TYPE), ValidationUtils.isAllowAggregation(TAG));
    }

    @Test
    public void testAllowAggregationByExpression() {
        Assert.assertEquals(List.of(DenialCause.AGGREGATION), ValidationUtils.isAllowAggregation(EXPRESSION));
    }

    @Test
    public void testAllowScaleByColumn() {
        Assert.assertEquals(List.of(), ValidationUtils.isAllowScale(COLUMN));
    }

    @Test
    public void testAllowScaleByTag() {
        Assert.assertEquals(List.of(DenialCause.TYPE), ValidationUtils.isAllowScale(TAG));
    }

    @Test
    public void testAllowScaleByExpression() {
        Assert.assertEquals(Arrays.asList(DenialCause.NOT_DETERMINISTIC, DenialCause.AGGREGATION, DenialCause.SCALE), ValidationUtils.isAllowScale(EXPRESSION));
    }

    @Test
    public void testAllowPrescaleByColumn() {
        Assert.assertEquals(List.of(), ValidationUtils.isAllowPrescale(COLUMN));
    }

    @Test
    public void testAllowPrescaleByTag() {
        Assert.assertEquals(List.of(), ValidationUtils.isAllowPrescale(TAG));
    }

    @Test
    public void testAllowPrescaleByExpression() {
        Assert.assertEquals(Arrays.asList(DenialCause.NOT_DETERMINISTIC, DenialCause.AGGREGATION, DenialCause.SCALE), ValidationUtils.isAllowPrescale(EXPRESSION));
    }

    @Test
    public void testAllowWhereByColumn() {
        Assert.assertEquals(List.of(), ValidationUtils.isAllowWhere(COLUMN));
    }

    @Test
    public void testAllowWhereByTag() {
        Assert.assertEquals(List.of(), ValidationUtils.isAllowWhere(TAG));
    }

    @Test
    public void testAllowWhereByExpression() {
        Assert.assertEquals(Arrays.asList(DenialCause.NOT_DETERMINISTIC, DenialCause.AGGREGATION), ValidationUtils.isAllowWhere(EXPRESSION));
    }

    @Test
    public void testAllowGroupingByColumn() {
        Assert.assertEquals(List.of(), ValidationUtils.isAllowGrouping(COLUMN));
    }

    @Test
    public void testAllowGroupingByTag() {
        Assert.assertEquals(List.of(DenialCause.TYPE), ValidationUtils.isAllowGrouping(TAG));
    }

    @Test
    public void testAllowGroupingByExpression() {
        Assert.assertEquals(Arrays.asList(DenialCause.NOT_DETERMINISTIC, DenialCause.AGGREGATION), ValidationUtils.isAllowGrouping(EXPRESSION));
    }

    @Test
    public void testAllowHavingByColumn() {
        Assert.assertEquals(List.of(), ValidationUtils.isAllowHaving(COLUMN));
    }

    @Test
    public void testAllowHavingByTag() {
        Assert.assertEquals(List.of(), ValidationUtils.isAllowHaving(TAG));
    }

    @Test
    public void testAllowHavingByExpression() {
        Assert.assertEquals(List.of(DenialCause.NOT_DETERMINISTIC), ValidationUtils.isAllowHaving(EXPRESSION));
    }

    @Test
    public void testAllowOrderByByColumn() {
        Assert.assertEquals(List.of(), ValidationUtils.isAllowOrderBy(COLUMN));
    }

    @Test
    public void testAllowOrderByByTag() {
        Assert.assertEquals(List.of(DenialCause.TYPE), ValidationUtils.isAllowOrderBy(TAG));
    }

    @Test
    public void testAllowOrderByByExpression() {
        Assert.assertEquals(List.of(DenialCause.NOT_DETERMINISTIC), ValidationUtils.isAllowOrderBy(EXPRESSION));
    }
}
