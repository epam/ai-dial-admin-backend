package com.epam.aidial.expressions;

public interface NumberConstant extends Constant {
    boolean isNegate();
    Number getNumberValue();
    NumberConstant negate();
}
