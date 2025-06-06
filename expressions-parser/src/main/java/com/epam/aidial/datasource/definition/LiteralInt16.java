package com.epam.aidial.datasource.definition;

public class LiteralInt16 extends LiteralInteger {
    private short value;

    public LiteralInt16(short value) {
        super(LiteralKind.INT16);
        this.value = value;
    }

    public short getValue() {
        return value;
    }

    @Override
    public LiteralInteger castTo(IntegralType type) {
        switch (type) {
            case INT8:
                if (value < Byte.MIN_VALUE || value > Byte.MAX_VALUE)
                    throw new ClassCastException("Value '" + value + "' cannot be safely cast to 'Int8'.");
                return new LiteralInt8((byte) value);

            case UINT8:
                if (value < 0 || value > 255)
                    throw new ClassCastException("Value '" + value + "' cannot be safely cast to 'UInt8'.");
                return new LiteralUInt8((byte) (value & 0xFF));

            case INT16:
                return this;

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
        return value + "i16";
    }
}
