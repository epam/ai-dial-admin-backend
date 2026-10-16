package com.epam.aidial.core.config.validation;

import com.networknt.schema.Error;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ValidationErrors {

    /**
     * Formats an error as {@code <instance path>: <message>} to keep the messages produced by networknt 1.x,
     * where the path was part of the message. Errors raised by us without a location keep the bare message.
     */
    public static String format(Error error) {
        return error.getInstanceLocation() == null ? error.getMessage() : error.toString();
    }
}
