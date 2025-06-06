package com.epam.aidial.datasource.definition;

public class LiteralUInt32 extends LiteralInteger {
    private int value;

    public LiteralUInt32(int value) {
        super(LiteralKind.UINT32);
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public LiteralInteger castTo(IntegralType type) {
        long longValue = value & 0xFFFFFFFFL;
        switch (type) {
            case INT8:
                if (longValue > Byte.MAX_VALUE)
                    throw new ClassCastException("Value '" + longValue + "' cannot be safely cast to 'Int8'.");
                return new LiteralInt8((byte) longValue);

            case UINT8:
                if (longValue > 0xFF)
                    throw new ClassCastException("Value '" + longValue + "' cannot be safely cast to 'UInt8'.");
                return new LiteralUInt8((byte) longValue);

            case INT16:
                if (longValue > Short.MAX_VALUE)
                    throw new ClassCastException("Value '" + longValue + "' cannot be safely cast to 'Int16'.");
                return new LiteralInt16((short) longValue);

            case UINT16:
                if (longValue > 0xFFFF)
                    throw new ClassCastException("Value '" + longValue + "' cannot be safely cast to 'UInt16'.");
                return this;

            case INT32:
                if (longValue > Integer.MAX_VALUE)
                    throw new ClassCastException("Value '" + longValue + "' cannot be safely cast to 'Int32'.");
                return new LiteralInt32((int) longValue);

            case UINT32:
                return this;

            case INT64:
                return new LiteralInt64(longValue);

            case UINT64:
                return new LiteralUInt64(longValue);

            default:
                throw new IllegalArgumentException("Undefined integral type.");
        }
    }

    @Override
    public String toString() {
        return Long.toString(value & 0xFFFFFFFFL) + "u32";
    }
}
