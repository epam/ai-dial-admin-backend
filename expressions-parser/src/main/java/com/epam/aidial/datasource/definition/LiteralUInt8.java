package com.epam.aidial.datasource.definition;

public final class LiteralUInt8 extends LiteralInteger {
    private byte value;

    public LiteralUInt8(byte value) {
        super(LiteralKind.UINT8);
        this.value = value;
    }

    public byte getValue() {
        return value;
    }

    @Override
    public LiteralInteger castTo(IntegralType type) {
        int intValue = value & 0xFF;
        switch (type) {
            case INT8:
                if (intValue > Byte.MAX_VALUE)
                    throw new ClassCastException("Value '" + intValue + "' cannot be safely cast to 'Int8'.");
                return new LiteralInt8((byte) intValue);

            case UINT8:
                return this;

            case INT16:
                return new LiteralInt16((short) intValue);

            case UINT16:
                return new LiteralUInt16((short) intValue);

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
        return Integer.toString(value & 0xFF) + "u8";
    }
}
