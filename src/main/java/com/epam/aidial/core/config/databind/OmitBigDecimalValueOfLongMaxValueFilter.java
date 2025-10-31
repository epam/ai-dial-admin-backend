package com.epam.aidial.core.config.databind;

import java.math.BigDecimal;

public class OmitBigDecimalValueOfLongMaxValueFilter {

    @Override
    public boolean equals(Object obj) {
        return obj == null || (obj instanceof BigDecimal bigDecimalObj && bigDecimalObj.equals(BigDecimal.valueOf(Long.MAX_VALUE)));
    }
}
