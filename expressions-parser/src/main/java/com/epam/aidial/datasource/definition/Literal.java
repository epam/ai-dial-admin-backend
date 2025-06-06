package com.epam.aidial.datasource.definition;

public abstract class Literal {
    private final LiteralKind kind;

    protected Literal(LiteralKind kind) {
        this.kind = kind;
    }

    public LiteralKind getKind() {
        return kind;
    }

    public Object getValueAsObject() {
        return switch (kind) {
            case BOOLEAN -> ((LiteralBoolean) this).getValue();
            case TEXT -> ((LiteralText) this).getValue();
            case INT8 -> ((LiteralInt8) this).getValue();
            case UINT8 -> ((LiteralUInt8) this).getValue();
            case INT16 -> ((LiteralInt16) this).getValue();
            case UINT16 -> ((LiteralUInt16) this).getValue();
            case INT32 -> ((LiteralInt32) this).getValue();
            case UINT32 -> ((LiteralUInt32) this).getValue();
            case INT64 -> ((LiteralInt64) this).getValue();
            case UINT64 -> ((LiteralUInt64) this).getValue();
            case FLOAT32 -> ((LiteralFloat32) this).getValue();
            case FLOAT64 -> ((LiteralFloat64) this).getValue();
            case DECIMAL -> ((LiteralDecimal) this).getValue();
            case NULL -> ((LiteralNull) this).getValue();
            case LIST -> ((LiteralList) this).getValue();
            default -> throw new IllegalStateException("Literal kind is unknown.");
        };
    }
}
