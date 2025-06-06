package com.epam.aidial.expressions.impl;

import com.epam.aidial.expressions.enums.Type;
import com.epam.aidial.expressions.NumberConstant;
import com.epam.aidial.expressions.TypeHelper;

public class NumberConstantImpl implements NumberConstant {
    private final Number value;
    private final Type type;
    private final boolean negate;

    private static final int MAX_UINT_8 = 0xff;
    private static final int MAX_UINT_16 = 0xffff;
    private static final long MAX_UINT_32 = 0xffffffffL;

    private NumberConstantImpl(Number value, Type type, boolean negate) {
        this.value = value;
        this.type = type;
        this.negate = negate;
    }

    public static NumberConstant valueOf(Long value) {
        if (value >= 0) {
            if (value <= MAX_UINT_8) {
                return new NumberConstantImpl(value.shortValue(), Type.UINT_8, false);
            } else if (value <= MAX_UINT_16) {
                return new NumberConstantImpl(value.intValue(), Type.UINT_16, false);
            } else if (value <= MAX_UINT_32) {
                return new NumberConstantImpl(value, Type.UINT_32, false);
            } else {
                return new NumberConstantImpl(value, Type.UINT_64, false);
            }
        } else {
            if (Byte.MIN_VALUE <= value) {
                return new NumberConstantImpl(-value.byteValue(), Type.INT_8, true);
            } else if (Short.MIN_VALUE <= value) {
                return new NumberConstantImpl(-value.shortValue(), Type.INT_16, true);
            } else if (Integer.MIN_VALUE <= value) {
                return new NumberConstantImpl(-value.intValue(), Type.INT_32, true);
            } else {
                return new NumberConstantImpl(-value, Type.INT_64, true);
            }
        }
    }

    public static NumberConstant valueOf(Double value) {
        return new NumberConstantImpl(Math.abs(value), Type.DOUBLE, value < 0);
    }

    @Override
    public Comparable getValue() {
        return TypeHelper.isReal(getType()) ? value.doubleValue() : value.longValue();
    }

    @Override
    public Number getNumberValue() {
        return value;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public boolean isNegate() {
        return negate;
    }

    public NumberConstant negate() {
        switch (type) {
            case FLOAT:
            case DOUBLE:
            case INT_8:
            case INT_16:
            case INT_32:
            case INT_64:
                return new NumberConstantImpl(value, type, !negate);
            case UINT_8:
            case UINT_16:
            case UINT_32:
            case UINT_64:
                return valueOf(-value.longValue());
            default:
                throw new IllegalStateException(type.name());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NumberConstantImpl)) return false;

        NumberConstantImpl that = (NumberConstantImpl) o;

        if (isNegate() != that.isNegate()) return false;
        if (!getValue().equals(that.getValue())) return false;
        return getType() == that.getType();
    }

    @Override
    public int hashCode() {
        int result = getValue().hashCode();
        result = 31 * result + getType().hashCode();
        result = 31 * result + (isNegate() ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return (negate ? "-" : "") + value;
    }
}
