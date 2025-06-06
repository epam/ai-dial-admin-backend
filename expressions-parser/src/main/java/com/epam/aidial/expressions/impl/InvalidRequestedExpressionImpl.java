package com.epam.aidial.expressions.impl;

import com.epam.aidial.expressions.InvalidRequestedExpression;
import com.epam.aidial.expressions.exceptions.ParseException;

public class InvalidRequestedExpressionImpl implements InvalidRequestedExpression {
    private final String requestString;
    private final String name;
    private final String message;
    private final ParseException.ErrorType errorType;

    public InvalidRequestedExpressionImpl(String requestString, String name, String message, ParseException.ErrorType errorType) {
        this.requestString = requestString;
        this.name = name;
        this.message = message;
        this.errorType = errorType;
    }

    @Override
    public String getRequestString() {
        return requestString;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public ParseException.ErrorType getErrorType() {
        return errorType;
    }

    @Override
    public String toString() {
        return "InvalidRequestedExpressionImpl{" +
                "requestString='" + requestString + '\'' +
                ", message='" + message + '\'' +
                ", errorType=" + errorType +
                '}';
    }
}
