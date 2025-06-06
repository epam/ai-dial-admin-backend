package com.epam.aidial.datasource.definition;

public class LiteralInt64 extends LiteralInteger {
    private long value;

    public LiteralInt64(long value) {
        super(LiteralKind.INT64);
        this.value = value;
    }

    public long getValue() {
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
                if (value < Short.MIN_VALUE || value > Short.MAX_VALUE)
                    throw new ClassCastException("Value '" + value + "' cannot be safely cast to 'Int16'.");
                return new LiteralInt16((short) value);

            case UINT16:
                if (value < 0 || value > 65536)
                    throw new ClassCastException("Value '" + value + "' cannot be safely cast to 'UInt16'.");
                return new LiteralUInt16((short) (value & 0xFFFF));

            case INT32:
                if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE)
                    throw new ClassCastException("Value '" + value + "' cannot be safely cast to 'Int32'.");
                return new LiteralInt32((int) value);

            case UINT32:
                if (value < 0 || value > 4294967295L)
                    throw new ClassCastException("Value '" + value + "' cannot be safely cast to 'UInt32'.");
                return new LiteralUInt32((int) (value & 0xFFFFFFFFL));

            case INT64:
                return this;

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
        return Long.toString(value) + "i64";
    }
}
