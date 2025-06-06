package com.epam.aidial.expressions.utils;

import com.epam.aidial.expressions.AggregationFunctionCall;
import com.epam.aidial.expressions.Alias;
import com.epam.aidial.expressions.Column;
import com.epam.aidial.expressions.Expression;
import com.epam.aidial.expressions.FunctionCall;
import com.epam.aidial.expressions.ScaleCall;
import com.epam.aidial.expressions.exceptions.ParseException;

public class ExpressionUtil {
    public static void validateExpression(final Expression expression) {
        validateExpression(expression, false, false);
    }

    private static final int CONTAIN_AGGREGATION_FUNCTION = 1;
    private static final int CONTAIN_COLUMN = 2;
    private static final int CONTAIN_ALL = CONTAIN_AGGREGATION_FUNCTION | CONTAIN_COLUMN;

    private static int validateExpression(final Expression expression,
                                          final boolean inAggregationFunction,
                                          final boolean inScaleCall) {
        int result = 0;
        if (expression instanceof AggregationFunctionCall) {
            final AggregationFunctionCall functionCall = (AggregationFunctionCall) expression;
            if (inAggregationFunction) {
                throw new ParseException(String.format("Aggregate function '%s' is found inside another aggregate function.",
                        functionCall.getFunction().getName()));
            }
            if (inScaleCall) {
                throw new ParseException(String.format("Aggregate function '%s' is found inside scale function.",
                    functionCall.getFunction().getName()));
            }
            for (final Expression arg : functionCall.getArgs()) {
                result |= validateExpression(arg, true, inScaleCall);
            }
            result = CONTAIN_AGGREGATION_FUNCTION;
        } else if (expression instanceof ScaleCall) {
            if (inScaleCall) {
                throw new ParseException(String.format("Scale function '%s' is found inside another scale function.",
                        ((ScaleCall) expression).getFunction().getName()));
            }
            for (final Expression arg : ((FunctionCall) expression).getArgs()) {
                if (!arg.isDeterministic()) {
                    throw new ParseException("Scale cannot be built by not deterministic expression");
                }
                result |= validateExpression(arg, inAggregationFunction, true);
            }
        } else if (expression instanceof FunctionCall) {
            for (final Expression arg : ((FunctionCall) expression).getArgs()) {
                result |= validateExpression(arg, inAggregationFunction, inScaleCall);
            }
        } else if (expression instanceof Alias) {
            result |= validateExpression(((Alias) expression).getExpression(), inAggregationFunction, inScaleCall);
        } else if (expression instanceof Column) {
            result |= CONTAIN_COLUMN;
        }

        if (result == CONTAIN_ALL) {
            throw new ParseException("Expression can't have column outside of aggregation function. " +
                    "Remove aggregation function or move column into aggregation function.");
        }

        return result;
    }
}
