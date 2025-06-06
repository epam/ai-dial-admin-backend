package com.epam.aidial.expressions;

import com.epam.aidial.expressions.exceptions.ParseException;

public interface InvalidRequestedExpression extends RequestedExpression {
    String getName();
    String getMessage();
    ParseException.ErrorType getErrorType();
}
