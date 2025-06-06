package com.epam.aidial.datasource.definition;

import com.google.common.primitives.UnsignedLongs;

public final class LiteralUInt64 extends LiteralInteger {
    private long value;

    public LiteralUInt64(long value) {
        super(LiteralKind.UINT64);
        this.value = value;
    }

    public long getValue() {
        return value;
    }

    @Override
    public LiteralInteger castTo(IntegralType type) {
        switch (type) {
            case INT8:
                if (value < 0 || value > Byte.MAX_VALUE)
                    throw new ClassCastException("Value '" + UnsignedLongs.toString(value) + "' cannot be safely cast to 'Int8'.");
                return new LiteralInt8((byte) value);

            case UINT8:
                if (value < 0 || value > 0xFF)
                    throw new ClassCastException("Value '" + UnsignedLongs.toString(value) + "' cannot be safely cast to 'UInt8'.");
                return new LiteralUInt8((byte) value);

            case INT16:
                if (value < 0 || value > Short.MAX_VALUE)
                    throw new ClassCastException("Value '" + UnsignedLongs.toString(value) + "' cannot be safely cast to 'Int16'.");
                return new LiteralInt16((short) value);

            case UINT16:
                if (value < 0 || value > 0xFFFF)
                    throw new ClassCastException("Value '" + UnsignedLongs.toString(value) + "' cannot be safely cast to 'UInt16'.");
                return this;

            case INT32:
                if (value < 0 || value > Integer.MAX_VALUE)
                    throw new ClassCastException("Value '" + UnsignedLongs.toString(value) + "' cannot be safely cast to 'Int32'.");
                return new LiteralInt32((int) value);

            case UINT32:
                if (value < 0 || value > Integer.MAX_VALUE)
                    throw new ClassCastException("Value '" + UnsignedLongs.toString(value) + "' cannot be safely cast to 'UInt32'.");
                return this;

            case INT64:
                if (value < 0)
                    throw new ClassCastException("Value '" + UnsignedLongs.toString(value) + "' cannot be safely cast to 'SInt64'.");
                return new LiteralInt64(value);

            case UINT64:
                return this;

            default:
                throw new IllegalArgumentException("Undefined integral type.");
        }
    }

    @Override
    public String toString() {
        return UnsignedLongs.toString(value) + "u64";
    }
}
