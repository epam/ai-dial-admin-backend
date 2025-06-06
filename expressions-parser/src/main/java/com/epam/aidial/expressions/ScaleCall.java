package com.epam.aidial.expressions;

public interface ScaleCall extends FunctionCall {
    @Override
    default boolean isScale() {
        return true;
    }
}
