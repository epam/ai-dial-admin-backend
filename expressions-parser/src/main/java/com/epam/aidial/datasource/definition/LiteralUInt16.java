package com.epam.aidial.datasource.definition;

public class LiteralUInt16 extends LiteralInteger {
    private short value;

    public LiteralUInt16(short value) {
        super(LiteralKind.UINT16);
        this.value = value;
    }

    public short getValue() {
        return value;
    }

    @Override
    public LiteralInteger castTo(IntegralType type) {
        int intValue = value & 0xFFFF;
        switch (type) {
            case INT8:
                if (intValue > Byte.MAX_VALUE)
                    throw new ClassCastException("Value '" + intValue + "' cannot be safely cast to 'Int8'.");
                return new LiteralInt8((byte) intValue);

            case UINT8:
                if (intValue > 0xFF)
                    throw new ClassCastException("Value '" + intValue + "' cannot be safely cast to 'UInt8'.");
                return new LiteralUInt8((byte) intValue);

            case INT16:
                if (intValue > Short.MAX_VALUE)
                    throw new ClassCastException("Value '" + intValue + "' cannot be safely cast to 'Int16'.");
                return new LiteralInt16((short) intValue);

            case UINT16:
                return this;

            case INT32:
                return new LiteralInt32(intValue);

            case UINT32:
                return new LiteralUInt32(intValue);

            case INT64:
                return new LiteralInt64(intValue);

            case UINT64:
                return new LiteralUInt64(intValue);

            default:
                throw new IllegalArgumentException("Undefined integral type.");
        }
    }

    @Override
    public String toString() {
        return Integer.toString(value & 0xFFFF) + "u16";
    }
}
