package com.epam.aidial.expressions.impl;

import com.epam.aidial.expressions.Constant;
import com.epam.aidial.expressions.enums.Type;

public class ConstantImpl implements Constant {
    public static final Constant ZERO = new ConstantImpl(Type.INT_32, 0);
    public static final Constant ONE = new ConstantImpl(Type.INT_32, 1);
    public static final Constant TWO = new ConstantImpl(Type.INT_32, 2);
    public static final Constant THOUSAND = new ConstantImpl(Type.INT_32, 1000);
    public static final Constant EMPTY_STRING = new ConstantImpl(Type.STRING, "");
    public static final Constant TRUE = new ConstantImpl(Type.BOOLEAN, true);
    public static final Constant FALSE = new ConstantImpl(Type.BOOLEAN, false);
    public static final Constant FLOAT_DELTA = new ConstantImpl(Type.FLOAT, 1e-5);
    public static final Constant DOUBLE_DELTA = new ConstantImpl(Type.DOUBLE, 1e-10);
    public static final Constant NULL = new ConstantImpl(Type.NOTHING, null);

    private final Type type;
    private final Comparable value;

    public ConstantImpl(Type type, Comparable value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public Comparable getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "" + value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConstantImpl)) return false;

        ConstantImpl constant = (ConstantImpl) o;

        if (getType() != constant.getType()) return false;
        return getValue() != null ? getValue().equals(constant.getValue()) : constant.getValue() == null;
    }

    @Override
    public int hashCode() {
        int result = getType().hashCode();
        result = 31 * result + (getValue() != null ? getValue().hashCode() : 0);
        return result;
    }
}
