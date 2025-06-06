package com.epam.aidial.expressions.impl;

import com.epam.aidial.datasource.definition.Decorator;
import com.epam.aidial.datasource.definition.EnumerationMemberDef;
import com.epam.aidial.datasource.definition.FormalParameterDef;
import com.epam.aidial.datasource.definition.InterfaceMethodDef;
import com.epam.aidial.expressions.AggregationFunctionCall;
import com.epam.aidial.expressions.Alias;
import com.epam.aidial.expressions.Column;
import com.epam.aidial.expressions.Constant;
import com.epam.aidial.expressions.Expression;
import com.epam.aidial.expressions.FunctionCall;
import com.epam.aidial.expressions.FunctionInfo;
import com.epam.aidial.expressions.FunctionsDatasource;
import com.epam.aidial.expressions.GroupFunctionCall;
import com.epam.aidial.expressions.NumberConstant;
import com.epam.aidial.expressions.TypeHelper;
import com.epam.aidial.expressions.enums.Type;
import com.epam.aidial.expressions.exceptions.ParseException;
import com.epam.aidial.expressions.parser.ExpressionsParser;
import com.epam.aidial.expressions.parser.ExpressionsParserBaseListener;
import com.epam.aidial.expressions.utils.ExpressionUtil;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ExpressionsParserListenerImpl extends ExpressionsParserBaseListener {
    private final Stack<Object> stack = new Stack<>();
    private final ExpressionParserContext context;
    private final FunctionsDatasource functions;

    private int numberOfErrors = 0;
    private Expression expression;

    public ExpressionsParserListenerImpl(final ExpressionParserContext context) {
        this.context = context;
        this.functions = context.getFunctions();
    }

    @Override
    public void visitErrorNode(ErrorNode node) {
        ++numberOfErrors;
    }

    public int getNumberOfErrors() {
        return numberOfErrors;
    }

    public Expression getExpression() {
        return expression;
    }

    @Override
    public void exitExpressionWithAlias(ExpressionsParser.ExpressionWithAliasContext ctx) {
        final String alias = (String) stack.pop();
        final Expression expression = (Expression) stack.pop();
        ExpressionUtil.validateExpression(expression);
        final Alias aliasExpr = new AliasImpl(alias, expression);
        this.expression = aliasExpr;
        context.addColumn(aliasExpr);
    }

    @Override
    public void exitExpressionWithoutAlias(ExpressionsParser.ExpressionWithoutAliasContext ctx) {
        final Expression expression = (Expression) stack.pop();
        ExpressionUtil.validateExpression(expression);
        this.expression = expression;
    }

    @Override
    public void enterAlias(ExpressionsParser.AliasContext ctx) {
        if (!context.isAllowAlias()) {
            throw new ParseException("Alias is not available here");
        }
        stack.push(ctx.getText());
    }

    @Override
    public void enterColumnNameExpression(ExpressionsParser.ColumnNameExpressionContext ctx) {
        final String name = ctx.getText();
        Column column;
        do {
            column = context.getColumn(name);
        } while (column == null && context.parseNext());
        if (column == null) {
            throw new ParseException(String.format("Report doesn't have column '%s'. There are only %s",
                    ctx.getText(), context.getColumnNames().stream().collect(Collectors.joining(", "))), ParseException.ErrorType.COLUMN_MISMATCH);
        }
        stack.add(column);
    }

    @Override
    public void enterFunction(ExpressionsParser.FunctionContext ctx) {
        stack.add(functions.getMethodsByName().get(ctx.getText()));
    }

    private String validateParam(final FormalParameterDef formal, final Expression actual) {
        final Type formalType = TypeHelper.of(formal.getType(), formal.getDecorators());
        if (!TypeHelper.isSubclass(actual.getType(), formalType)) {
            return "argument mismatch; " + actual.getType() + " cannot be converted to " + formalType;
        }
        if (TypeHelper.hasConstDecorator(formal) || TypeHelper.getParamDecorator(formal) != null) {
            if (!(actual instanceof Constant)) {
                return "argument mismatch; " + actual.getType() + " should be constant";
            }
            final Decorator minDecorator = TypeHelper.getMinDecorator(formal);
            final Constant constant = (Constant) actual;
            if (constant instanceof NumberConstant) {
                if (minDecorator != null) {
                    final Object min = TypeHelper.getDecoratorValue(minDecorator, formalType, "Value").get();
                    final boolean inclusive = (boolean) TypeHelper.getDecoratorValue(minDecorator, formalType, "Inclusive").get();
                    final int result = compare(actual, min);
                    if (inclusive && result < 0 || !inclusive && result <= 0) {
                        return "argument mismatch; '" + constant.getValue() + "' must be " + (inclusive ? ">=" : ">") + " " + min;
                    }
                }
                final Decorator maxDecorator = TypeHelper.getMaxDecorator(formal);
                if (maxDecorator != null) {
                    final Object max = TypeHelper.getDecoratorValue(maxDecorator, formalType, "Value").get();
                    final boolean inclusive = (boolean) TypeHelper.getDecoratorValue(maxDecorator, formalType, "Inclusive").get();
                    final int result = compare(actual, max);
                    if (inclusive && result > 0 || !inclusive && result >= 0) {
                        return "argument mismatch; '" + constant.getValue() + "' must be " + (inclusive ? "<=" : "<") + " " + max;
                    }
                }
            } else {
                final Decorator maxSizeDecorator = TypeHelper.getMaxSizeDecorator(formal);
                if (maxSizeDecorator != null) {
                    final int size = (int) TypeHelper.getDecoratorValue(maxSizeDecorator, formalType, "Value").get();
                    if (constant.getValue().toString().length() > size) {
                        return "argument mismatch; length of '" + constant.getValue() + "' must be <= " + size;
                    }
                }
                final Class<Enum> aEnum = TypeHelper.getEnumDecorator(formal);
                if (aEnum != null) {
                    final String value = String.valueOf(constant.getValue());
                    final Enum[] values = aEnum.getEnumConstants();
                    if (value == null || Arrays.stream(values).noneMatch(x -> x.name().equals(value))) {
                        return "argument mismatch; '" + constant.getValue() + "' must be of " +
                               Stream.of(values).map(x -> "'" + x.name() + "'").collect(Collectors.joining(", "));
                    }
                }
            }
        }
        return null;
    }

    /**
     * @return null if correct, error reason otherwise.
     */
    private String isCorrectParams(final List<FormalParameterDef> formals, final List<Expression> actual) {
        final boolean parameterArray = !formals.isEmpty() && formals.get(formals.size() - 1).isParameterArray();
        final int n = parameterArray ? formals.size() - 1 : formals.size();

        if ((!parameterArray && formals.size() != actual.size()) ||
            (parameterArray && n > actual.size())) {
            return "actual and formal argument lists differ in length";
        }

        for (int i = 0; i < n; i++) {
            final String status = validateParam(formals.get(i), actual.get(i));
            if (status != null) {
                return status;
            }
        }

        if (parameterArray) {
            final FormalParameterDef formal = formals.get(n);
            final Decorator minSizeDecorator = TypeHelper.getMinSizeDecorator(formal);
            if (minSizeDecorator != null) {
                final Type formalType = TypeHelper.of(formal.getType(), formal.getDecorators());
                final int size = (int) TypeHelper.getDecoratorValue(minSizeDecorator, formalType, "Value").get();
                if (actual.size() - n + 1 < size) {
                    return "argument mismatch; size of vararg must be >= " + size;
                }
            }
            final Decorator maxSizeDecorator = TypeHelper.getMaxSizeDecorator(formal);
            if (maxSizeDecorator != null) {
                final Type formalType = TypeHelper.of(formal.getType(), formal.getDecorators());
                final int size = (int) TypeHelper.getDecoratorValue(maxSizeDecorator, formalType, "Value").get();
                if (actual.size() - n + 1 > size) {
                    return "argument mismatch; size of vararg must be <= " + size;
                }
            }
            for (int i = n; i < actual.size(); i++) {
                final String status = validateParam(formal, actual.get(i));
                if (status != null) {
                    return status;
                }
            }
        }

        return null;
    }

    private static int compare(final Expression arg,
                               final Object param) {
        return new BigDecimal(arg.toString()).compareTo(new BigDecimal(param.toString()));
    }

    private static FunctionInfo toDateTime = new FunctionImpl("toDateTime", Type.TIMESTAMP);
    private static FunctionInfo toDate = new FunctionImpl("toDate", Type.TIMESTAMP);
    private static FunctionInfo divide = new FunctionImpl("divide", Type.DOUBLE);
    private static FunctionInfo multiply = new FunctionImpl("multiply", Type.TIMESTAMP);
    private static FunctionInfo toInt64 = new FunctionImpl("toInt64", Type.TIMESTAMP);
    private static FunctionInfo toString = new FunctionImpl("toString", Type.STRING);

    private String paramTypeToString(FormalParameterDef param) {
        return (TypeHelper.hasConstDecorator(param) ? "const " : "") + TypeHelper.of(param.getType(), param.getDecorators());
    }

    private String paramTypeToString(Expression param) {
        return (param instanceof Constant ? "const " : "") + param.getType();
    }

    private String generateIncorrectFunctionCallMessage(final List<Expression> args, final List<InterfaceMethodDef> methods, final List<String> reasons) {
        final StringBuilder sb = new StringBuilder("Found function ")
                .append(methods.get(0).getName()).append("(")
                .append(args.stream().map(this::paramTypeToString).collect(Collectors.joining(", ")))
                .append(")\n")
                .append("Errors:");
        for (int i = 0; i < methods.size(); i++) {
            final InterfaceMethodDef method = methods.get(i);
            sb.append("\nfunction ").append(method.getName()).append("(")
                    .append(method.getFormalParameters().stream().map(this::paramTypeToString).collect(Collectors.joining(", ")))
                    .append(") is not applicable (")
                    .append(reasons.get(i))
                    .append(")");
        }
        return sb.toString();
    }

    private FunctionCall createFunctionCall(final List<InterfaceMethodDef> methods,
                                            final List<Expression> args) {
        final List<String> reasons = new ArrayList<>();

        for (final InterfaceMethodDef method : methods) {
            final String reason = isCorrectParams(method.getFormalParameters(), args);
            if (reason != null) {
                reasons.add(reason);
            } else {
                final Type type = TypeHelper.of(method.getReturnType(), method.getDecorators());

                final List<Expression> processedArgs = new ArrayList<>();

                for (int argIndex = 0, argDefIndex = 0; argIndex < args.size(); argIndex++) {
                    final FormalParameterDef argDef = method.getFormalParameters().get(argDefIndex);
                    Expression arg = args.get(argIndex);
                    final Decorator formatDecorator = TypeHelper.getDecoratorByName(argDef.getDecorators(), "ClickHouseDateFormat");
                    final Class<Enum> enumClass = TypeHelper.getEnumDecorator(argDef);
                    if (formatDecorator == null && enumClass == null) {
                        processedArgs.add(arg);
                    } else if (enumClass != null) {
                        processedArgs.add(new ConstantImpl(Type.ENUM, Enum.valueOf(enumClass, ((Constant) arg).getValue().toString())));
                    } else {
                        arg = new FunctionCallImpl(toDateTime, new FunctionCallImpl(divide, arg, ConstantImpl.THOUSAND));
                        final String format = ((EnumerationMemberDef) TypeHelper.getDecoratorValue(formatDecorator, Type.ENUM, "Value").get()).getFullName();
                        switch (format) {
                            case "DateFormat.Date":
                                processedArgs.add(new FunctionCallImpl(toDate, arg));
                                break;
                            case "DateFormat.DateTime":
                                processedArgs.add(arg);
                                break;
                            default:
                                throw new IllegalStateException(format);
                        }
                    }
                    if (argDefIndex + 1 < method.getFormalParameters().size()) {
                        ++argDefIndex;
                    }
                }

                final FunctionCall function;
                if (TypeHelper.getDecoratorByName(method.getDecorators(), "Scale") == null) {
                    final boolean deterministic = TypeHelper.getDecoratorByName(method.getDecorators(), "NotDeterministic") == null;
                    function = new FunctionCallImpl(new FunctionImpl(method.getName(), type, deterministic), processedArgs);
                } else {
                    function = new ScaleCallImpl(new FunctionImpl(method.getName(), type), processedArgs);
                }

                final Optional<String> dateFormat = TypeHelper.getDateFormat(method.getDecorators());
                if (dateFormat.isPresent()) {
                    switch (dateFormat.get()) {
                        case "DateFormat.Date":
                            return new FunctionCallImpl(multiply, new FunctionCallImpl(toInt64, new FunctionCallImpl(toDateTime, function)), ConstantImpl.THOUSAND);
                        case "DateFormat.DateTime":
                            return new FunctionCallImpl(multiply, new FunctionCallImpl(toInt64, function), ConstantImpl.THOUSAND);
                        default:
                            throw new IllegalStateException(dateFormat.get());
                    }
                }

                return function;
            }
        }

        throw new ParseException(generateIncorrectFunctionCallMessage(args, methods, reasons));
    }

    private Pair<List<Constant>, List<Expression>> splitParamsAndArgs(final List<FormalParameterDef> argDef, final List<Expression> args) {
        final List<Constant> constants = new ArrayList<>();
        final List<Expression> expressions = new ArrayList<>();
        for (int argIndex = 0, argDefIndex = 0; argIndex < args.size(); argIndex++) {
            if (TypeHelper.getParamDecorator(argDef.get(argDefIndex)) == null) {
                expressions.add(args.get(argIndex));
            } else {
                constants.add((Constant) args.get(argIndex));
            }
            if (argDefIndex + 1 < argDef.size()) {
                ++argDefIndex;
            }
        }
        return Pair.of(constants, expressions);
    }

    private AggregationFunctionCall createAggregationFunctionCall(final List<InterfaceMethodDef> methods,
                                                                  final List<Expression> args) {
        final List<String> reasons = new ArrayList<>();

        for (final InterfaceMethodDef method : methods) {
            final String reason = isCorrectParams(method.getFormalParameters(), args);
            if (reason != null) {
                reasons.add(reason);
            } else {
                final Type type = TypeHelper.of(method.getReturnType(), method.getDecorators());
                final Pair<List<Constant>, List<Expression>> t = splitParamsAndArgs(method.getFormalParameters(), args);
                final boolean deterministic = TypeHelper.getDecoratorByName(method.getDecorators(), "NotDeterministic") == null;
                return new AggregationFunctionCallImpl(new FunctionImpl(method.getName(), type, deterministic), t.getLeft(), t.getRight());
            }
        }

        throw new ParseException(generateIncorrectFunctionCallMessage(args, methods, reasons));
    }

    private GroupFunctionCall createGroupFunctionCall(final List<InterfaceMethodDef> methods,
                                                      final List<Expression> args) {
        final List<String> reasons = new ArrayList<>();

        for (final InterfaceMethodDef method : methods) {
            final String reason = isCorrectParams(method.getFormalParameters(), args);
            if (reason != null) {
                reasons.add(reason);
            } else {
                final Type type = TypeHelper.of(method.getReturnType(), method.getDecorators());
                final Pair<List<Constant>, List<Expression>> t = splitParamsAndArgs(method.getFormalParameters(), args);
                final boolean deterministic = TypeHelper.getDecoratorByName(method.getDecorators(), "NotDeterministic") == null;
                return new GroupFunctionCallImpl(new FunctionImpl(method.getName(), type, deterministic), t.getLeft(), t.getRight());
            }
        }

        throw new ParseException(generateIncorrectFunctionCallMessage(args, methods, reasons));
    }

    @Override
    public void exitFunctionCall(ExpressionsParser.FunctionCallContext ctx) {
        final List<Expression> args = (List<Expression>) stack.pop();
        final List<InterfaceMethodDef> methods = (List<InterfaceMethodDef>) stack.pop();

        stack.add(createFunctionCall(methods, args));
    }

    @Override
    public void enterAggregation_function(ExpressionsParser.Aggregation_functionContext ctx) {
        stack.add(functions.getAggregationMethodsByName().get(ctx.getText()));
    }

    @Override
    public void exitAggregationFunctionCall(ExpressionsParser.AggregationFunctionCallContext ctx) {
        final List<Expression> args = (List<Expression>) stack.pop();
        final List<InterfaceMethodDef> methods = (List<InterfaceMethodDef>) stack.pop();

        stack.add(createAggregationFunctionCall(methods, args));
    }

    @Override
    public void enterGroup_function(ExpressionsParser.Group_functionContext ctx) {
        stack.add(functions.getGroupMethodsByName().get(ctx.getText()));
    }

    @Override
    public void exitGroupFunctionCall(ExpressionsParser.GroupFunctionCallContext ctx) {
        final List<Expression> args = (List<Expression>) stack.pop();
        final List<InterfaceMethodDef> methods = (List<InterfaceMethodDef>) stack.pop();

        stack.add(createGroupFunctionCall(methods, args));
    }

    private <T> void processArgs(final Class<T> clazz) {
        if (!clazz.isInstance(stack.peek())) {
            stack.add(Collections.<T>emptyList());
        } else {
            final List<T> params = new ArrayList<>();
            while (clazz.isInstance(stack.peek())) {
                params.add((T) stack.pop());
            }
            Collections.reverse(params);
            stack.add(params);
        }
    }

    @Override
    public void exitFunction_params(ExpressionsParser.Function_paramsContext ctx) {
        processArgs(Constant.class);
    }

    @Override
    public void exitFunction_args(ExpressionsParser.Function_argsContext ctx) {
        processArgs(Expression.class);
    }

    @Override
    public void exitNegateOperator(ExpressionsParser.NegateOperatorContext ctx) {
        final List<Expression> args = Collections.singletonList((Expression) stack.pop());
        stack.add(createFunctionCall(functions.getNegateMethods(), args));
    }

    @Override
    public void exitUnaryMinusOperator(ExpressionsParser.UnaryMinusOperatorContext ctx) {
        final Expression expr = (Expression) stack.pop();
        if (expr instanceof NumberConstant) {
            final NumberConstant number = (NumberConstant) expr;
            stack.add(number.negate());
        } else {
            stack.add(createFunctionCall(functions.getNegateMethods(), Collections.singletonList(expr)));
        }
    }

    private void addDualExpression() {
        final Expression b = (Expression) stack.pop();
        final List<InterfaceMethodDef> methods = (List<InterfaceMethodDef>) stack.pop();
        final Expression a = (Expression) stack.pop();
        addDualExpression(methods, a, b);
    }

    private void addDualExpression(List<InterfaceMethodDef> methods) {
        final Expression b = (Expression) stack.pop();
        final Expression a = (Expression) stack.pop();
        addDualExpression(methods, a, b);
    }

    private void addDualExpression(List<InterfaceMethodDef> methods, Expression a, Expression b) {
        final List<Expression> args = Arrays.asList(a, b);
        stack.add(createFunctionCall(methods, args));
    }

    @Override
    public void exitMultiplicativeExpression(ExpressionsParser.MultiplicativeExpressionContext ctx) {
        addDualExpression();
    }

    @Override
    public void exitAdditiveExpression(ExpressionsParser.AdditiveExpressionContext ctx) {
        addDualExpression();
    }

    @Override
    public void exitComparisonExpression(ExpressionsParser.ComparisonExpressionContext ctx) {
        addDualExpression();
    }

    @Override
    public void exitLogicalNotExpression(ExpressionsParser.LogicalNotExpressionContext ctx) {
        final List<Expression> args = Collections.singletonList((Expression) stack.pop());
        stack.add(createFunctionCall(functions.getNotMethods(), args));
    }

    @Override
    public void exitLogicalAndExpression(ExpressionsParser.LogicalAndExpressionContext ctx) {
        addDualExpression(functions.getAndMethods());
    }

    @Override
    public void exitLogicalOrExpression(ExpressionsParser.LogicalOrExpressionContext ctx) {
        addDualExpression(functions.getOrMethods());
    }

    @Override
    public void exitTernaryOperator(ExpressionsParser.TernaryOperatorContext ctx) {
        final Expression c = (Expression) stack.pop();
        final Expression b = (Expression) stack.pop();
        final Expression a = (Expression) stack.pop();
        final List<Expression> args = Arrays.asList(a, b, c);
        stack.add(createFunctionCall(functions.getIfMethods(), args));
    }

    private List<InterfaceMethodDef> resolveComparisonOperator(final String operator) {
        switch (operator) {
            case "=":
            case "==":
                return functions.getEqualsMethods();
            case "<>":
            case "!=":
                return functions.getNotEqualsMethods();
            case "<":
                return functions.getLessMethods();
            case ">":
                return functions.getGreaterMethods();
            case "<=":
                return functions.getLessOrEqualsMethods();
            case ">=":
                return functions.getGreaterOrEqualsMethods();
        }
        throw new IllegalStateException(operator);
    }

    @Override
    public void enterComparison_operator(ExpressionsParser.Comparison_operatorContext ctx) {
        stack.add(resolveComparisonOperator(ctx.getText()));
    }

    private List<InterfaceMethodDef> resolveAdditiveOperator(final char operator) {
        switch (operator) {
            case '+':
                return functions.getPlusMethods();
            case '-':
                return functions.getMinusMethods();
        }
        throw new IllegalStateException(String.valueOf(operator));
    }

    @Override
    public void enterAdditive_operator(ExpressionsParser.Additive_operatorContext ctx) {
        stack.add(resolveAdditiveOperator(ctx.getText().charAt(0)));
    }

    private List<InterfaceMethodDef> resolveMultiplicativeOperator(final char operator) {
        switch (operator) {
            case '*':
                return functions.getMultiplyMethods();
            case '/':
                return functions.getDivideMethods();
            case '%':
                return functions.getModuloMethods();
        }
        throw new IllegalStateException(String.valueOf(operator));
    }

    @Override
    public void enterMultiplicative_operator(ExpressionsParser.Multiplicative_operatorContext ctx) {
        stack.add(resolveMultiplicativeOperator(ctx.getText().charAt(0)));
    }

    @Override
    public void enterDecimal_literal(ExpressionsParser.Decimal_literalContext ctx) {
        try {
            stack.add(NumberConstantImpl.valueOf(Long.valueOf(ctx.getText())));
        } catch (NumberFormatException e) {
            stack.add(NumberConstantImpl.valueOf(Double.valueOf(ctx.getText())));
        }
    }

    @Override
    public void enterRealNumber(ExpressionsParser.RealNumberContext ctx) {
        stack.add(NumberConstantImpl.valueOf(Double.valueOf(ctx.getText())));
    }

    @Override
    public void enterNan(ExpressionsParser.NanContext ctx) {
        stack.add(NumberConstantImpl.valueOf(Double.NaN));
    }

    @Override
    public void enterInf(ExpressionsParser.InfContext ctx) {
        stack.add(NumberConstantImpl.valueOf(Double.POSITIVE_INFINITY));
    }

    @Override
    public void enterString_literal(ExpressionsParser.String_literalContext ctx) {
        final String sWithQuotes = ctx.getText();
        final String s = sWithQuotes.substring(1, sWithQuotes.length() - 1);
        try {
            stack.add(new ConstantImpl(Type.TIMESTAMP, Instant.parse(s).toEpochMilli()));
        } catch (final DateTimeParseException e) {
            try {
                stack.add(new ConstantImpl(Type.INTERVAL, Duration.parse(s).toMillis()));
            } catch (final DateTimeParseException ex) {
                try {
                    stack.add(new ConstantImpl(Type.UUID, UUID.fromString(s)));
                } catch (final IllegalArgumentException exx) {
                    stack.add(new ConstantImpl(Type.STRING, s));
                }
            }
        }
    }

    @Override
    public void enterBoolean_literal(ExpressionsParser.Boolean_literalContext ctx) {
        stack.add(new ConstantImpl(Type.BOOLEAN, Boolean.valueOf(ctx.getText())));
    }

    @Override
    public void enterHexadecimal_literal(ExpressionsParser.Hexadecimal_literalContext ctx) {
        stack.add(NumberConstantImpl.valueOf(Long.decode(ctx.getText())));
    }

    @Override
    public void enterNull_literal(ExpressionsParser.Null_literalContext ctx) {
        stack.add(ConstantImpl.NULL);
    }

    @Override
    public void enterBinary_literal(ExpressionsParser.Binary_literalContext ctx) {
        final String stringWithQuates = ctx.getText();
        final String hexString = stringWithQuates.substring(2, stringWithQuates.length() - 1);
        final byte[] bytes;
        try {
            bytes = Hex.decodeHex(hexString);
        } catch (final DecoderException e) {
            throw new ParseException(e.getMessage(), ParseException.ErrorType.INCORRECT_SYNTAX);
        }
        stack.add(new BinaryConstantImpl(bytes));
    }
}
