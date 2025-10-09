package com.epam.aidial.core.config.databind;

public class OmitLongMaxValueFilter {

    @Override
    public boolean equals(Object obj) {
        return obj == null || (obj instanceof Long longObj && longObj == Long.MAX_VALUE);
    }
}
