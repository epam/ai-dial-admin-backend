package com.epam.aidial.datasource.definition;

public final class LiteralFloat32 extends Literal {
    private final float value;

    public LiteralFloat32(float value) {
        super(LiteralKind.FLOAT32);
        this.value = value;
    }

    public float getValue() {
        return value;
    }

    @Override
    public String toString() {
        if (Double.isNaN(value))
            return "Float32.NaN";
        if (Double.isInfinite(value))
            return value > 0 ? "Float32.POSITIVE_INFINITY" : "Float32.NEGATIVE_INFINITY";
        return Float.toString(value) + "f32";
    }
}
