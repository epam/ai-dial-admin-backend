package com.epam.aidial.expressions.exceptions;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ParseException extends RuntimeException {
    private final ErrorType errorType;

    public ParseException(String message, ErrorType errorType) {
        super(message);
        this.errorType = errorType;
    }

    public ParseException(String message) {
        this(message, ErrorType.INCORRECT_SYNTAX);
    }

    public ParseException(String message, Throwable cause, ErrorType errorType) {
        super(message, cause);
        this.errorType = errorType;
    }

    public ParseException(String message, Throwable cause) {
        this(message, cause, ErrorType.INCORRECT_SYNTAX);
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    public enum ErrorType {
        @JsonProperty("IncorrectSyntax")
        INCORRECT_SYNTAX,
        @JsonProperty("ColumnMismatch")
        COLUMN_MISMATCH
    }
}
