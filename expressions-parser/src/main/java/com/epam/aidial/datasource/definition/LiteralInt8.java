package com.epam.aidial.datasource.definition;

public class LiteralInt8 extends LiteralInteger {
    private final byte value;

    public LiteralInt8(byte value) {
        super(LiteralKind.INT8);
        this.value = value;
    }

    public byte getValue() {
        return value;
    }

    @Override
    public LiteralInteger castTo(IntegralType type) {
        switch (type) {
            case INT8:
                return this;

            case UINT8:
                if (value < 0)
                    throw new ClassCastException("Value '" + value + "' cannot be safely cast to 'UInt8'.");
                return new LiteralUInt8(value);

            case INT16:
                return new LiteralInt16(value);

            case UINT16:
                if (value < 0)
                    throw new ClassCastException("Value '" + value + "' cannot be safely cast to 'UInt16'.");
                return new LiteralUInt16(value);

            case INT32:
                return new LiteralInt32(value);

            case UINT32:
                if (value < 0)
                    throw new ClassCastException("Value '" + value + "' cannot be safely cast to 'UInt32'.");
                return new LiteralUInt32(value);

            case INT64:
                return new LiteralInt64(value);

            case UINT64:
                if (value < 0)
                    throw new ClassCastException("Value '" + value + "' cannot be safely cast to 'UInt64'.");
                return new LiteralUInt64(value);

            default:
                throw new IllegalArgumentException("Undefined integral type.");
        }
    }

    @Override
    public String toString() {
        return value + "i8";
    }
}
