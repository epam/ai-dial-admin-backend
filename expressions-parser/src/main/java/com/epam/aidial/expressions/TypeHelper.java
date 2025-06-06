package com.epam.aidial.expressions;

import com.epam.aidial.datasource.definition.Decorator;
import com.epam.aidial.datasource.definition.EnumerationMemberDef;
import com.epam.aidial.datasource.definition.FormalParameterDef;
import com.epam.aidial.datasource.definition.TypeKind;
import com.epam.aidial.expressions.enums.Type;
import com.epam.aidial.expressions.stubs.ReportPropertyType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class TypeHelper {
    private static final Map<Type, Set<Type>> subclasses;

    static {
        subclasses = new HashMap<>();
        subclasses.put(Type.DOUBLE, new HashSet<>(Arrays.asList(
                Type.INT_8,
                Type.UINT_8,
                Type.INT_16,
                Type.UINT_16,
                Type.INT_32,
                Type.UINT_32,
                Type.INT_64,
                Type.UINT_64,
                Type.FLOAT
        )));
        subclasses.put(Type.FLOAT, new HashSet<>(Arrays.asList(
                Type.INT_8,
                Type.UINT_8,
                Type.INT_16,
                Type.UINT_16
        )));
        subclasses.put(Type.INT_64, new HashSet<>(Arrays.asList(
                Type.INT_8,
                Type.UINT_8,
                Type.INT_16,
                Type.UINT_16,
                Type.INT_32,
                Type.UINT_32,
                Type.UINT_64
        )));
        subclasses.put(Type.UINT_64, new HashSet<>(Arrays.asList(
                Type.UINT_8,
                Type.UINT_16,
                Type.UINT_32
        )));
        subclasses.put(Type.INT_32, new HashSet<>(Arrays.asList(
                Type.INT_8,
                Type.UINT_8,
                Type.INT_16,
                Type.UINT_16,
                Type.UINT_32
        )));
        subclasses.put(Type.UINT_32, new HashSet<>(Arrays.asList(
                Type.UINT_8,
                Type.UINT_16
        )));
        subclasses.put(Type.INT_16, new HashSet<>(Arrays.asList(
                Type.INT_8,
                Type.UINT_8,
                Type.UINT_16
        )));
        subclasses.put(Type.UINT_16, new HashSet<>(Arrays.asList(
                Type.UINT_8
        )));
        subclasses.put(Type.INT_8, new HashSet<>(Arrays.asList(
                Type.UINT_8
        )));
        subclasses.put(Type.UINT_8, new HashSet<>(Arrays.asList(
        )));
    }

    public static Type of(final com.epam.aidial.datasource.definition.Type type, final List<Decorator> decorators) {
        return type == null ? null : of(type.getKind(), decorators);
    }

    private static Type of(final TypeKind kind, final List<Decorator> decorators) {
        switch (kind) {
            case UINT8:
                return Type.UINT_8;
            case INT8:
                return Type.INT_8;
            case UINT16:
                return Type.UINT_16;
            case INT16:
                return Type.INT_16;
            case UINT32:
                return Type.UINT_32;
            case INT32:
                return Type.INT_32;
            case UINT64:
                return Type.UINT_64;
            case INT64:
                return Type.INT_64;
            case FLOAT32:
                return Type.FLOAT;
            case FLOAT64:
                return Type.DOUBLE;
            case TIMESTAMP:
                return Type.TIMESTAMP;
            case DURATION:
                return Type.INTERVAL;
            case TEXT:
                return Type.STRING;
            case UUID:
                return Type.UUID;
            case BOOLEAN:
                return Type.BOOLEAN;
            case DATA:
                return Type.BINARY;
            default:
                throw new IllegalStateException(kind.name());
        }
    }

    public static Type of(final ReportPropertyType kind) {
        switch (kind) {
            case UINT_8:
                return Type.UINT_8;
            case INT_8:
                return Type.INT_8;
            case UINT_16:
                return Type.UINT_16;
            case INT_16:
                return Type.INT_16;
            case UINT_32:
                return Type.UINT_32;
            case INT_32:
                return Type.INT_32;
            case UINT_64:
                return Type.UINT_64;
            case INT_64:
                return Type.INT_64;
            case FLOAT:
                return Type.FLOAT;
            case DOUBLE:
                return Type.DOUBLE;
            case TIMESTAMP:
                return Type.TIMESTAMP;
            case INTERVAL:
                return Type.INTERVAL;
            case STRING:
                return Type.STRING;
            case UUID:
                return Type.UUID;
            case BOOLEAN:
                return Type.BOOLEAN;
            default:
                throw new IllegalStateException(kind.name());
        }
    }

    public static Object toType(final Type type, final Object value) {
        switch (type) {
            case DOUBLE:
                return Double.valueOf(value.toString());
            default:
                throw new IllegalStateException(String.format("Type: %s, Value: %s", type.name(), value));
        }
    }

    public static boolean isSubclass(final Type subclass, final Type parent) {
        if (subclass == parent) {
            return true;
        }

        if (subclass == Type.NOTHING) {
            return true;
        }

        final Set<Type> s = subclasses.get(parent);

        return s != null && s.contains(subclass);
    }

    public static List<Type> getSubclasses(final Type parent) {
        final Set<Type> t = subclasses.get(parent);
        if (t == null) {
            return Collections.singletonList(parent);
        }
        final List<Type> list = new ArrayList<>(t);
        list.add(parent);
        return list;
    }

    public static List<Type> getSubclasses(final TypeKind kind, final List<Decorator> decorators) {
        return getSubclasses(of(kind, decorators));
    }

    public static Decorator getDecoratorByName(final List<Decorator> decorators, final String name) {
        return decorators.stream().filter(x -> x.getName().equals(name)).findAny().orElse(null);
    }

    public static Decorator getParamDecorator(FormalParameterDef param) {
        return getDecoratorByName(param.getDecorators(), "Parameter");
    }

    public static Decorator getMinDecorator(FormalParameterDef param) {
        return getDecoratorByName(param.getDecorators(), "Min");
    }

    public static Decorator getMaxDecorator(FormalParameterDef param) {
        return getDecoratorByName(param.getDecorators(), "Max");
    }

    public static boolean hasConstDecorator(FormalParameterDef param) {
        return getDecoratorByName(param.getDecorators(), "Constant") != null;
    }

    public static Decorator getMaxSizeDecorator(FormalParameterDef param) {
        return getDecoratorByName(param.getDecorators(), "MaxSize");
    }

    public static Decorator getMinSizeDecorator(FormalParameterDef param) {
        return getDecoratorByName(param.getDecorators(), "MinSize");
    }

    public static Class<Enum> getEnumDecorator(FormalParameterDef param) {
        final Decorator decorator = getDecoratorByName(param.getDecorators(), "Enum");
        if (decorator == null) {
            return null;
        }
        final String fullName = getDecoratorValue(decorator, Type.ENUM, "Value").get().toString();
        final int lastPoint = fullName.lastIndexOf(".");
        try {
            return (Class<Enum>) Class.forName(fullName.substring(0, lastPoint).toLowerCase() + fullName.substring(lastPoint));
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    public static Optional<Object> getDecoratorValue(final Decorator decorator, Type type, final String decoratorValueName) {
        return decorator.getArguments().stream()
                .filter(x -> x.getName().equals(decoratorValueName))
                .findFirst()
                .map(x -> x.getValue().getValueAsObject());
    }

    public static Optional<String> getDateFormat(final List<Decorator> decorators) {
        final Decorator decorator = getDecoratorByName(decorators, "ClickHouseDateFormat");
        if (decorator == null) {
            return Optional.empty();
        }
        return getDecoratorValue(decorator, Type.STRING, "Value").map(x -> ((EnumerationMemberDef) x).getFullName());
    }

    private static int getOrdinal(final Type x) {
        switch (x) {
            case DOUBLE:
            case FLOAT:
            case INT_64:
            case INT_32:
            case INT_16:
            case INT_8:
            case UINT_64:
            case UINT_32:
            case UINT_16:
            case UINT_8:
                return 0;
            case INTERVAL:
                return 1;
            case TIMESTAMP:
                return 2;
            case ENUM:
            case STRING:
            case CHAR:
                return 3;
            case BOOLEAN:
                return 4;
            case UUID:
                return 5;
            case BINARY:
                return 6;
            default:
                return Integer.MAX_VALUE;
        }
    }

    public static int compare(final Type a, final Type b) {
        return getOrdinal(a) - getOrdinal(b);
    }

    public static boolean isReal(final Type x) {
        switch (x) {
            case DOUBLE:
            case FLOAT:
                return true;
            default:
                return false;
        }
    }

    public static boolean isNumber(final Type x) {
        switch (x) {
            case DOUBLE:
            case FLOAT:
            case INT_64:
            case INT_32:
            case INT_16:
            case INT_8:
            case UINT_64:
            case UINT_32:
            case UINT_16:
            case UINT_8:
                return true;
            default:
                return false;
        }
    }
}
