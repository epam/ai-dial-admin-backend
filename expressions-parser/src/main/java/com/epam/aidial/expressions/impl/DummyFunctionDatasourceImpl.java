package com.epam.aidial.expressions.impl;

import com.epam.aidial.datasource.definition.Decorator;
import com.epam.aidial.datasource.definition.DecoratorPropertyValue;
import com.epam.aidial.datasource.definition.FormalParameterDef;
import com.epam.aidial.datasource.definition.InterfaceMethodDef;
import com.epam.aidial.datasource.definition.LiteralBoolean;
import com.epam.aidial.datasource.definition.LiteralInt64;
import com.epam.aidial.datasource.definition.LiteralText;
import com.epam.aidial.datasource.definition.Type;
import com.epam.aidial.datasource.definition.TypeBoolean;
import com.epam.aidial.datasource.definition.TypeFloat32;
import com.epam.aidial.datasource.definition.TypeFloat64;
import com.epam.aidial.datasource.definition.TypeInt16;
import com.epam.aidial.datasource.definition.TypeInt32;
import com.epam.aidial.datasource.definition.TypeInt64;
import com.epam.aidial.datasource.definition.TypeUInt64;
import com.epam.aidial.expressions.FunctionsDatasource;

import java.util.List;
import java.util.Map;

public class DummyFunctionDatasourceImpl implements FunctionsDatasource {
    private final Map<String, List<InterfaceMethodDef>> methodsByName;
    private final Map<String, List<InterfaceMethodDef>> aggregationMethodsByName;
    private final List<InterfaceMethodDef> negateMethods;
    private final List<InterfaceMethodDef> andMethods;
    private final List<InterfaceMethodDef> orMethods;
    private final List<InterfaceMethodDef> notMethods;
    private final List<InterfaceMethodDef> ifMethods;
    private final List<InterfaceMethodDef> plusMethods;
    private final List<InterfaceMethodDef> minusMethods;
    private final List<InterfaceMethodDef> multiplyMethods;
    private final List<InterfaceMethodDef> divideMethods;
    private final List<InterfaceMethodDef> moduloMethods;
    private final List<InterfaceMethodDef> equalsMethods;
    private final List<InterfaceMethodDef> notEqualsMethods;
    private final List<InterfaceMethodDef> lessMethods;
    private final List<InterfaceMethodDef> greaterMethods;
    private final List<InterfaceMethodDef> lessOrEqualsMethods;
    private final List<InterfaceMethodDef> greaterOrEqualsMethods;

    {
        var plusInt32 = createMethodDef("plus", TypeInt32.INSTANCE, TypeInt32.INSTANCE, TypeInt32.INSTANCE);
        var plusInt64 = createMethodDef("plus", TypeInt64.INSTANCE, TypeInt64.INSTANCE, TypeInt64.INSTANCE);
        var plusFloat32 = createMethodDef("plus", TypeFloat32.INSTANCE, TypeFloat32.INSTANCE, TypeFloat32.INSTANCE);
        var plusFloat64 = createMethodDef("plus", TypeFloat64.INSTANCE, TypeFloat64.INSTANCE, TypeFloat64.INSTANCE);

        var minusInt32 = createMethodDef("minus", TypeInt32.INSTANCE, TypeInt32.INSTANCE, TypeInt32.INSTANCE);
        var minusInt64 = createMethodDef("minus", TypeInt64.INSTANCE, TypeInt64.INSTANCE, TypeInt64.INSTANCE);
        var minusFloat32 = createMethodDef("minus", TypeFloat32.INSTANCE, TypeFloat32.INSTANCE, TypeFloat32.INSTANCE);
        var minusFloat64 = createMethodDef("minus", TypeFloat64.INSTANCE, TypeFloat64.INSTANCE, TypeFloat64.INSTANCE);

        var moduloInt32 = createMethodDef("modulo", TypeInt32.INSTANCE, TypeInt32.INSTANCE, TypeInt32.INSTANCE);

        var absInt32 = createMethodDef("abs", TypeInt32.INSTANCE, TypeInt32.INSTANCE);
        var absInt64 = createMethodDef("abs", TypeInt64.INSTANCE, TypeInt64.INSTANCE);
        var absFloat = createMethodDef("abs", TypeFloat32.INSTANCE, TypeFloat32.INSTANCE);

        var roundDouble = createMethodDef("round", TypeFloat64.INSTANCE, TypeFloat64.INSTANCE, TypeInt64.INSTANCE);
        {
            roundDouble.getFormalParameters().get(1).getDecorators().add(
                    new Decorator("Constant", List.of())
            );
        }

        var negateInt32 = createMethodDef("negate", TypeInt32.INSTANCE, TypeInt32.INSTANCE);
        var negateDouble = createMethodDef("negate", TypeFloat64.INSTANCE, TypeFloat64.INSTANCE);

        var multiplyInt32 = createMethodDef("multiply", TypeInt32.INSTANCE, TypeInt32.INSTANCE, TypeInt32.INSTANCE);

        var divideDouble = createMethodDef("divide", TypeFloat64.INSTANCE, TypeFloat64.INSTANCE, TypeFloat64.INSTANCE);

        var andBoolean = createMethodDef("and", TypeBoolean.INSTANCE, TypeBoolean.INSTANCE, TypeBoolean.INSTANCE);
        var orBoolean = createMethodDef("or", TypeBoolean.INSTANCE, TypeBoolean.INSTANCE, TypeBoolean.INSTANCE);
        var notBoolean = createMethodDef("not", TypeBoolean.INSTANCE, TypeBoolean.INSTANCE);

        var equalsInt64 = createMethodDef("equals", TypeBoolean.INSTANCE, TypeInt64.INSTANCE, TypeInt64.INSTANCE);
        var notEqualsInt64 = createMethodDef("notEquals", TypeBoolean.INSTANCE, TypeInt64.INSTANCE, TypeInt64.INSTANCE);

        var lessInt32 = createMethodDef("less", TypeBoolean.INSTANCE, TypeInt32.INSTANCE, TypeInt32.INSTANCE);
        var lessInt64 = createMethodDef("less", TypeBoolean.INSTANCE, TypeInt64.INSTANCE, TypeInt64.INSTANCE);
        var lessFloat32 = createMethodDef("less", TypeBoolean.INSTANCE, TypeFloat32.INSTANCE, TypeFloat32.INSTANCE);
        var lessFloat64 = createMethodDef("less", TypeBoolean.INSTANCE, TypeFloat64.INSTANCE, TypeFloat64.INSTANCE);

        var greaterInt32 = createMethodDef("greater", TypeBoolean.INSTANCE, TypeInt32.INSTANCE, TypeInt32.INSTANCE);
        var greaterInt64 = createMethodDef("greater", TypeBoolean.INSTANCE, TypeInt64.INSTANCE, TypeInt64.INSTANCE);
        var greaterFloat32 = createMethodDef("greater", TypeBoolean.INSTANCE, TypeFloat32.INSTANCE, TypeFloat32.INSTANCE);
        var greaterFloat64 = createMethodDef("greater", TypeBoolean.INSTANCE, TypeFloat64.INSTANCE, TypeFloat64.INSTANCE);

        var lessOrEqualsInt32 = createMethodDef("lessOrEquals", TypeBoolean.INSTANCE, TypeInt32.INSTANCE, TypeInt32.INSTANCE);
        var lessOrEqualsInt64 = createMethodDef("lessOrEquals", TypeBoolean.INSTANCE, TypeInt64.INSTANCE, TypeInt64.INSTANCE);
        var lessOrEqualsFloat32 = createMethodDef("lessOrEquals", TypeBoolean.INSTANCE, TypeFloat32.INSTANCE, TypeFloat32.INSTANCE);
        var lessOrEqualsFloat64 = createMethodDef("lessOrEquals", TypeBoolean.INSTANCE, TypeFloat64.INSTANCE, TypeFloat64.INSTANCE);

        var greaterOrEqualsInt32 = createMethodDef("greaterOrEquals", TypeBoolean.INSTANCE, TypeInt32.INSTANCE, TypeInt32.INSTANCE);
        var greaterOrEqualsInt64 = createMethodDef("greaterOrEquals", TypeBoolean.INSTANCE, TypeInt64.INSTANCE, TypeInt64.INSTANCE);
        var greaterOrEqualsFloat32 = createMethodDef("greaterOrEquals", TypeBoolean.INSTANCE, TypeFloat32.INSTANCE, TypeFloat32.INSTANCE);
        var greaterOrEqualsFloat64 = createMethodDef("greaterOrEquals", TypeBoolean.INSTANCE, TypeFloat64.INSTANCE, TypeFloat64.INSTANCE);

        var ifBoolean = createMethodDef("if", TypeBoolean.INSTANCE, TypeBoolean.INSTANCE, TypeBoolean.INSTANCE, TypeBoolean.INSTANCE);
        var ifInt32 = createMethodDef("if", TypeInt32.INSTANCE, TypeBoolean.INSTANCE, TypeInt32.INSTANCE, TypeInt32.INSTANCE);
        var ifInt64 = createMethodDef("if", TypeInt64.INSTANCE, TypeBoolean.INSTANCE, TypeInt64.INSTANCE, TypeInt64.INSTANCE);

        var intervalScaleNumberFloat64 = createMethodDef("intervalScaleNumber", TypeInt32.INSTANCE, TypeFloat64.INSTANCE, TypeInt16.INSTANCE);
        {
            intervalScaleNumberFloat64.getDecorators().addAll(List.of(
                    new Decorator("Group", List.of(
                            new DecoratorPropertyValue("Value", new LiteralText("Scales"), false)
                    )),
                    new Decorator("Scale", List.of())
            ));
            intervalScaleNumberFloat64.getFormalParameters().get(1).getDecorators().addAll(List.of(
                    new Decorator("Constant", List.of()),
                    new Decorator("Min", List.of(
                            new DecoratorPropertyValue("Value", new LiteralInt64(1), false),
                            new DecoratorPropertyValue("Inclusive", LiteralBoolean.TRUE, true)
                    ))
            ));
        }

        methodsByName = Map.ofEntries(
                Map.entry("plus", List.of(plusInt32, plusInt64, plusFloat32, plusFloat64)),
                Map.entry("minus", List.of(minusInt32, minusInt64, minusFloat32, minusFloat64)),
                Map.entry("modulo", List.of(moduloInt32)),
                Map.entry("abs", List.of(absInt32, absInt64, absFloat)),
                Map.entry("round", List.of(roundDouble)),
                Map.entry("negate", List.of(negateInt32, negateDouble)),
                Map.entry("multiply", List.of(multiplyInt32)),
                Map.entry("divide", List.of(divideDouble)),
                Map.entry("and", List.of(andBoolean)),
                Map.entry("or", List.of(orBoolean)),
                Map.entry("not", List.of(notBoolean)),
                Map.entry("equals", List.of(equalsInt64)),
                Map.entry("notEquals", List.of(notEqualsInt64)),
                Map.entry("less", List.of(lessInt32, lessInt64, lessFloat32, lessFloat64)),
                Map.entry("greater", List.of(greaterInt32, greaterInt64, greaterFloat32, greaterFloat64)),
                Map.entry("lessOrEquals", List.of(lessOrEqualsInt32, lessOrEqualsInt64, lessOrEqualsFloat32, lessOrEqualsFloat64)),
                Map.entry("greaterOrEquals", List.of(greaterOrEqualsInt32, greaterOrEqualsInt64, greaterOrEqualsFloat32, greaterOrEqualsFloat64)),
                Map.entry("if", List.of(ifBoolean, ifInt32, ifInt64)),
                Map.entry("intervalScaleNumber", List.of(intervalScaleNumberFloat64))
        );
    }

    {
        var count = createMethodDef("count", TypeInt64.INSTANCE);

        var sumInt64 = createMethodDef("sum", TypeInt64.INSTANCE, TypeInt64.INSTANCE);
        var sumUint64 = createMethodDef("sum", TypeUInt64.INSTANCE, TypeUInt64.INSTANCE);
        var sumFloat64 = createMethodDef("sum", TypeFloat64.INSTANCE, TypeFloat64.INSTANCE);

        var avgFloat64 = createMethodDef("avg", TypeFloat64.INSTANCE, TypeFloat64.INSTANCE);

        var quantileFloat64 = new InterfaceMethodDef("quantile", TypeFloat64.INSTANCE, null);
        {
            quantileFloat64.getDecorators().add(new Decorator("NotDeterministic", List.of()));

            var levelParam = new FormalParameterDef(quantileFloat64, "level", TypeFloat64.INSTANCE, false, null);
            levelParam.getDecorators().addAll(List.of(
                    new Decorator("Parameter", List.of(
                            new DecoratorPropertyValue("Default", new LiteralText("0.95"), false)
                    )),
                    new Decorator("Min", List.of(
                            new DecoratorPropertyValue("Value", new LiteralInt64(0), false),
                            new DecoratorPropertyValue("Inclusive", LiteralBoolean.TRUE, true)
                    )),
                    new Decorator("Max", List.of(
                            new DecoratorPropertyValue("Value", new LiteralInt64(1), false),
                            new DecoratorPropertyValue("Inclusive", LiteralBoolean.TRUE, true)
                    ))
            ));
            quantileFloat64.getFormalParameters().add(levelParam);

            var xParam = new FormalParameterDef(quantileFloat64, "x", TypeFloat64.INSTANCE, false, null);
            quantileFloat64.getFormalParameters().add(xParam);
        }

        aggregationMethodsByName = Map.of(
                "count", List.of(count),
                "sum", List.of(sumInt64, sumUint64, sumFloat64),
                "avg", List.of(avgFloat64),
                "quantile", List.of(quantileFloat64)
        );
    }

    public DummyFunctionDatasourceImpl() {
        negateMethods = methodsByName.get("negate");
        andMethods = methodsByName.get("and");
        orMethods = methodsByName.get("or");
        notMethods = methodsByName.get("not");
        ifMethods = methodsByName.get("if");
        plusMethods = methodsByName.get("plus");
        minusMethods = methodsByName.get("minus");
        multiplyMethods = methodsByName.get("multiply");
        divideMethods = methodsByName.get("divide");
        moduloMethods = methodsByName.get("modulo");

        equalsMethods = methodsByName.get("equals");
        notEqualsMethods = methodsByName.get("notEquals");
        lessMethods = methodsByName.get("less");
        greaterMethods = methodsByName.get("greater");
        lessOrEqualsMethods = methodsByName.get("lessOrEquals");
        greaterOrEqualsMethods = methodsByName.get("greaterOrEquals");
    }

    @Override
    public Map<String, List<InterfaceMethodDef>> getMethodsByName() {
        return methodsByName;
    }

    @Override
    public Map<String, List<InterfaceMethodDef>> getAggregationMethodsByName() {
        return aggregationMethodsByName;
    }

    @Override
    public Map<String, List<InterfaceMethodDef>> getGroupMethodsByName() {
        return Map.of(); //todo: temporary
    }

    @Override
    public List<InterfaceMethodDef> getNegateMethods() {
        return negateMethods;
    }

    @Override
    public List<InterfaceMethodDef> getAndMethods() {
        return andMethods;
    }

    @Override
    public List<InterfaceMethodDef> getOrMethods() {
        return orMethods;
    }

    @Override
    public List<InterfaceMethodDef> getNotMethods() {
        return notMethods;
    }

    @Override
    public List<InterfaceMethodDef> getIfMethods() {
        return ifMethods;
    }

    @Override
    public List<InterfaceMethodDef> getPlusMethods() {
        return plusMethods;
    }

    @Override
    public List<InterfaceMethodDef> getMinusMethods() {
        return minusMethods;
    }

    @Override
    public List<InterfaceMethodDef> getMultiplyMethods() {
        return multiplyMethods;
    }

    @Override
    public List<InterfaceMethodDef> getDivideMethods() {
        return divideMethods;
    }

    @Override
    public List<InterfaceMethodDef> getModuloMethods() {
        return moduloMethods;
    }

    @Override
    public List<InterfaceMethodDef> getEqualsMethods() {
        return equalsMethods;
    }

    @Override
    public List<InterfaceMethodDef> getNotEqualsMethods() {
        return notEqualsMethods;
    }

    @Override
    public List<InterfaceMethodDef> getLessMethods() {
        return lessMethods;
    }

    @Override
    public List<InterfaceMethodDef> getGreaterMethods() {
        return greaterMethods;
    }

    @Override
    public List<InterfaceMethodDef> getLessOrEqualsMethods() {
        return lessOrEqualsMethods;
    }

    @Override
    public List<InterfaceMethodDef> getGreaterOrEqualsMethods() {
        return greaterOrEqualsMethods;
    }

    private static InterfaceMethodDef createMethodDef(String name, Type returnType, Type... paramTypes) {
        var methodDef = new InterfaceMethodDef(name, returnType, null);
        for (int i = 0; i < paramTypes.length; i++) {
            methodDef.getFormalParameters().add(new FormalParameterDef(methodDef, "param" + i, paramTypes[i], false, null));
        }
        return methodDef;
    }

}
