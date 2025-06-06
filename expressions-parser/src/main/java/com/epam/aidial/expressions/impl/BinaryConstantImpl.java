package com.epam.aidial.expressions.impl;

import com.epam.aidial.expressions.Constant;
import com.epam.aidial.expressions.enums.Type;

public class BinaryConstantImpl implements Constant {
    private final ComparableByteArray value;

    public BinaryConstantImpl(final byte[] value) {
        this.value = new ComparableByteArray(value);
    }

    public BinaryConstantImpl(final ComparableByteArray value) {
        this.value = value;
    }

    @Override
    public ComparableByteArray getValue() {
        return value;
    }

    @Override
    public Type getType() {
        return Type.BINARY;
    }
}
