package com.epam.aidial.ql.helpers;

import com.epam.aidial.expressions.Expression;
import com.epam.aidial.expressions.TypeHelper;
import com.epam.aidial.expressions.enums.Type;
import com.epam.aidial.ql.common.model.enums.BinaryComparisonOperator;
import com.epam.aidial.ql.common.model.enums.DenialCause;
import com.epam.aidial.ql.exceptions.ValidationException;
import com.epam.aidial.ql.model.Completable;
import com.epam.aidial.ql.model.Tuple;
import org.apache.commons.lang3.NotImplementedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.epam.aidial.ql.common.model.enums.BinaryComparisonOperator.CONTAINS;
import static com.epam.aidial.ql.common.model.enums.BinaryComparisonOperator.ENDS_WITH;
import static com.epam.aidial.ql.common.model.enums.BinaryComparisonOperator.EQUALS;
import static com.epam.aidial.ql.common.model.enums.BinaryComparisonOperator.GREATER;
import static com.epam.aidial.ql.common.model.enums.BinaryComparisonOperator.GREATER_OR_EQUALS;
import static com.epam.aidial.ql.common.model.enums.BinaryComparisonOperator.IN;
import static com.epam.aidial.ql.common.model.enums.BinaryComparisonOperator.LESS;
import static com.epam.aidial.ql.common.model.enums.BinaryComparisonOperator.LESS_OR_EQUALS;
import static com.epam.aidial.ql.common.model.enums.BinaryComparisonOperator.LIKE;
import static com.epam.aidial.ql.common.model.enums.BinaryComparisonOperator.NOT_CONTAINS;
import static com.epam.aidial.ql.common.model.enums.BinaryComparisonOperator.NOT_EQUALS;
import static com.epam.aidial.ql.common.model.enums.BinaryComparisonOperator.NOT_IN;
import static com.epam.aidial.ql.common.model.enums.BinaryComparisonOperator.NOT_LIKE;
import static com.epam.aidial.ql.common.model.enums.BinaryComparisonOperator.STARTS_WITH;

public class ValidationUtils {
    private static final Map<Type, Set<BinaryComparisonOperator>> operators;
    private static final Set<Type> allowedTypesForScales;

    static {
        operators = Map.ofEntries(
                Map.entry(Type.BOOLEAN, Set.of(EQUALS, NOT_EQUALS, IN, NOT_IN)),
                Map.entry(Type.INT_8, Set.of(EQUALS, NOT_EQUALS, IN, NOT_IN, LESS, LESS_OR_EQUALS, GREATER, GREATER_OR_EQUALS)),
                Map.entry(Type.UINT_8, Set.of(EQUALS, NOT_EQUALS, IN, NOT_IN, LESS, LESS_OR_EQUALS, GREATER, GREATER_OR_EQUALS)),
                Map.entry(Type.INT_16, Set.of(EQUALS, NOT_EQUALS, IN, NOT_IN, LESS, LESS_OR_EQUALS, GREATER, GREATER_OR_EQUALS)),
                Map.entry(Type.UINT_16, Set.of(EQUALS, NOT_EQUALS, IN, NOT_IN, LESS, LESS_OR_EQUALS, GREATER, GREATER_OR_EQUALS)),
                Map.entry(Type.INT_32, Set.of(EQUALS, NOT_EQUALS, IN, NOT_IN, LESS, LESS_OR_EQUALS, GREATER, GREATER_OR_EQUALS)),
                Map.entry(Type.UINT_32, Set.of(EQUALS, NOT_EQUALS, IN, NOT_IN, LESS, LESS_OR_EQUALS, GREATER, GREATER_OR_EQUALS)),
                Map.entry(Type.INT_64, Set.of(EQUALS, NOT_EQUALS, IN, NOT_IN, LESS, LESS_OR_EQUALS, GREATER, GREATER_OR_EQUALS)),
                Map.entry(Type.UINT_64, Set.of(EQUALS, NOT_EQUALS, IN, NOT_IN, LESS, LESS_OR_EQUALS, GREATER, GREATER_OR_EQUALS)),
                Map.entry(Type.FLOAT, Set.of(EQUALS, NOT_EQUALS, LESS, LESS_OR_EQUALS, GREATER, GREATER_OR_EQUALS)),
                Map.entry(Type.DOUBLE, Set.of(EQUALS, NOT_EQUALS, LESS, LESS_OR_EQUALS, GREATER, GREATER_OR_EQUALS)),
                Map.entry(Type.ENUM, Set.of(EQUALS, NOT_EQUALS, IN, NOT_IN)),
                Map.entry(Type.CHAR, Set.of(EQUALS, NOT_EQUALS, IN, NOT_IN, LESS, LESS_OR_EQUALS, GREATER, GREATER_OR_EQUALS)),
                Map.entry(Type.TIMESTAMP, Set.of(EQUALS, NOT_EQUALS, IN, NOT_IN, LESS, LESS_OR_EQUALS, GREATER, GREATER_OR_EQUALS)),
                Map.entry(Type.INTERVAL, Set.of(EQUALS, NOT_EQUALS, IN, NOT_IN, LESS, LESS_OR_EQUALS, GREATER, GREATER_OR_EQUALS)),
                Map.entry(Type.STRING, Set.of(EQUALS, NOT_EQUALS, IN, NOT_IN, LIKE, NOT_LIKE, STARTS_WITH, ENDS_WITH, CONTAINS, NOT_CONTAINS)),
                Map.entry(Type.UUID, Set.of(EQUALS, NOT_EQUALS, IN, NOT_IN)),
                Map.entry(Type.TUPLE, Set.of(EQUALS, NOT_EQUALS, IN, NOT_IN)),
                Map.entry(Type.BINARY, Set.of(EQUALS, NOT_EQUALS, IN, NOT_IN)),
                Map.entry(Type.NOTHING, Set.of(BinaryComparisonOperator.values()))
        );

        allowedTypesForScales = Set.of(
                Type.INT_8,
                Type.UINT_8,
                Type.INT_16,
                Type.UINT_16,
                Type.INT_32,
                Type.UINT_32,
                Type.INT_64,
                Type.UINT_64,
                Type.FLOAT,
                Type.DOUBLE,
                Type.INTERVAL,
                Type.TIMESTAMP
        );
    }

    private static String buildStringCauses(final List<DenialCause> causes) {
        return causes.stream().map(x -> x.name().toLowerCase().replace('_', ' ')).collect(Collectors.joining(", "));
    }

    private static void verifyAllow(final List<DenialCause> causes, final String clause, final String name) {
        if (!causes.isEmpty()) {
            throw new ValidationException(String.format("%s by `%s` is not allowed. Causes: %s.", clause, name, buildStringCauses(causes)));
        }
    }

    public static List<DenialCause> isAllowGrouping(final Expression expression) {
        final List<DenialCause> causes = new ArrayList<>();

        final Type type = expression.getType();
        if (!expression.isDeterministic()) {
            causes.add(DenialCause.NOT_DETERMINISTIC);
        }
        if (expression.isAggregation()) {
            causes.add(DenialCause.AGGREGATION);
        }
        if (!expression.isScale()) {
            if (type == Type.DOUBLE || type == Type.FLOAT || type == Type.TAG) {
                causes.add(DenialCause.TYPE);
            }
        }

        return causes;
    }

    public static void verifyAllowGrouping(final Expression expression, final String name) {
        verifyAllow(isAllowGrouping(expression), "Group", name);
    }

    public static List<DenialCause> isAllowHaving(final Expression expression) {
        final List<DenialCause> causes = new ArrayList<>();
        if (!expression.isDeterministic()) {
            causes.add(DenialCause.NOT_DETERMINISTIC);
        }
        return causes;
    }

    public static void verifyAllowHaving(final Expression expression, final String name) {
        verifyAllow(isAllowHaving(expression), "Having", name);
    }

    public static List<DenialCause> isAllowWhere(final Expression expression) {
        final List<DenialCause> causes = isAllowHaving(expression);
        if (expression.isAggregation()) {
            causes.add(DenialCause.AGGREGATION);
        }
        return causes;
    }

    public static void verifyAllowWhere(final Expression expression, final String name) {
        verifyAllow(isAllowWhere(expression), "Where", name);
    }

    public static List<DenialCause> isAllowPrescale(final Expression expression) {
        final List<DenialCause> causes = isAllowWhere(expression);
        if (expression.isScale()) {
            causes.add(DenialCause.SCALE);
        }
        return causes;
    }

    public static void verifyAllowPrescale(final Expression expression, final String name) {
        verifyAllow(isAllowPrescale(expression), "Prescale", name);
    }

    public static List<DenialCause> isAllowOrderBy(final Expression expression) {
        final List<DenialCause> causes = new ArrayList<>();
        if (expression.getType() == Type.TAG) {
            causes.add(DenialCause.TYPE);
        }
        if (!expression.isDeterministic()) {
            causes.add(DenialCause.NOT_DETERMINISTIC);
        }
        return causes;
    }

    public static void verifyAllowOrderBy(final Expression expression, final String name) {
        verifyAllow(isAllowOrderBy(expression), "Order", name);
    }

    public static List<DenialCause> isAllowScale(final Expression expression) {
        final List<DenialCause> causes = new ArrayList<>();
        final Type type = expression.getType();
        if (!expression.isDeterministic()) {
            causes.add(DenialCause.NOT_DETERMINISTIC);
        }
        if (expression.isAggregation()) {
            causes.add(DenialCause.AGGREGATION);
        }
        if (expression.isScale()) {
            causes.add(DenialCause.SCALE);
        }
        if (!allowedTypesForScales.contains(type)) {
            causes.add(DenialCause.TYPE);
        }
        return causes;
    }

    public static List<DenialCause> isAllowAggregation(final Expression expression) {
        final List<DenialCause> causes = new ArrayList<>();
        if (expression.getType() == Type.TAG) {
            causes.add(DenialCause.TYPE);
        }
        if (expression.isAggregation()) {
            causes.add(DenialCause.AGGREGATION);
        }
        return causes;
    }

    public static List<DenialCause> isAllowExpressions(final Expression expression) {
        final List<DenialCause> causes = new ArrayList<>();
        if (expression.getType() == Type.TAG) {
            causes.add(DenialCause.TYPE);
        }
        return causes;
    }

    private static List<Expression> getTupleExpressions(final Expression expression) {
        if (expression instanceof Completable) {
            return ((Completable) expression).getExpressions();
        } else if (expression instanceof Tuple) {
            return ((Tuple) expression).getExpressions();
        }
        throw new NotImplementedException(expression.toString());
    }

    private static String getFullType(final Expression expression) {
        if (expression.getType() != Type.TUPLE) {
            return expression.getType().name();
        }
        return getTupleExpressions(expression).stream().map(ValidationUtils::getFullType).collect(Collectors.joining(", ", "(", ")"));
    }

    private static boolean isSubType(final BinaryComparisonOperator operator,
                                     final Expression child,
                                     final Expression parent) {
        final Type parentType = parent.getType();
        if (parentType == Type.TUPLE) {
            if (child.getType() != Type.TUPLE) {
                return false;
            }
            final List<Expression> parentExpressions = getTupleExpressions(parent);
            final List<Expression> childExpressions = getTupleExpressions(child);
            if (parentExpressions.size() != childExpressions.size()) {
                return false;
            }
            for (int i = 0; i < parentExpressions.size(); i++) {
                if (!isSubType(operator, childExpressions.get(i), parentExpressions.get(i))) {
                    return false;
                }
            }
            return true;
        } else if (!(TypeHelper.isSubclass(child.getType(), parentType)
                || parentType == Type.ENUM && child.getType() == Type.STRING
                || parentType == Type.STRING && child.getType() == Type.UUID
                || parentType == Type.UUID && child.getType() == Type.STRING)) {
            return false;
        }
        Set<BinaryComparisonOperator> typeOperators = operators.get(parentType);
        return typeOperators != null && typeOperators.contains(operator);
    }

    public static void verifySupportOperator(final BinaryComparisonOperator operator,
                                             final Expression left,
                                             final Expression right,
                                             final String leftName,
                                             final String rightName) {
        if (right.getType() == Type.TUPLE && (operator == IN || operator == NOT_IN)) {
            if (right instanceof Completable) {
                if (!isSubType(operator, right, left)) {
                    throw new ValidationException(String.format(
                            "Comparision `%s` (%s) and `%s` (%s) types using `%s` operator is unsupported.",
                            getFullType(left), leftName, getFullType(right), rightName, operator
                    ));
                }
            } else if (right instanceof Tuple) {
                if (!isSubType(operator, right, left)) {
                    ((Tuple) right).getExpressions().forEach(expression -> {
                        if (!isSubType(operator, expression, left)) {
                            throw new ValidationException(String.format(
                                    "Comparision `%s` (%s) and `%s` (%s) types using `%s` operator is unsupported.",
                                    getFullType(left), leftName, getFullType(right), rightName, operator
                            ));
                        }
                    });
                }
            } else {
                throw new NotImplementedException(right.toString());
            }
        } else {
            if (!isSubType(operator, left, right) && !isSubType(operator, right, left)) {
                throw new ValidationException(String.format(
                        "Comparision `%s` (%s) and `%s` (%s) types using `%s` operator is unsupported.",
                        getFullType(left), leftName, getFullType(right), rightName, operator
                ));
            }
        }
    }
}
